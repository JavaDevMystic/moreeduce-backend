package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.WithdrawalRequestDto;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.entity.WithdrawalRequest;
import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.repository.WithdrawalRequestRepository;
import bunyodbek.uz.moreeduce.service.WithdrawalService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WithdrawalRequestDto createWithdrawalRequest(WithdrawalRequestDto requestDto, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        BigDecimal amount = requestDto.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        // Balansni tekshirish
        if (teacher.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance.");
        }

        // Balansni "muzlatish"
        teacher.setAvailableBalance(teacher.getAvailableBalance().subtract(amount));
        teacher.setPendingBalance(teacher.getPendingBalance().add(amount));
        userRepository.save(teacher);

        WithdrawalRequest newRequest = WithdrawalRequest.builder()
                .teacher(teacher)
                .amount(amount)
                .status(WithdrawalStatus.PENDING)
                .build();

        return mapToDto(requestRepository.save(newRequest));
    }

    @Override
    public List<WithdrawalRequestDto> getAllWithdrawalRequests() {
        return List.of();
    }

    @Override
    public Page<WithdrawalRequestDto> getAllWithdrawalRequests(WithdrawalStatus status, Pageable pageable) {
        Specification<WithdrawalRequest> spec = (root, query, cb) -> {
            if (status != null) {
                return cb.equal(root.get("status"), status);
            }
            return cb.conjunction(); // Agar status null bo'lsa, hech qanday filtr qo'llanilmaydi
        };
        return requestRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Override
    public List<WithdrawalRequestDto> getMyWithdrawalRequests(String teacherEmail) {
        User teacher = getUserByEmail(teacherEmail);
        return requestRepository.findByTeacherId(teacher.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveWithdrawalRequest(Long requestId) {
        WithdrawalRequest request = getRequestById(requestId);
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved.");
        }

        User teacher = request.getTeacher();
        // "Muzlatilgan" balansni kamaytirish (pul to'landi)
        teacher.setPendingBalance(teacher.getPendingBalance().subtract(request.getAmount()));
        userRepository.save(teacher);

        request.setStatus(WithdrawalStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    @Override
    public void rejectWithdrawalRequest(Long requestId) {

    }

    @Override
    @Transactional
    public void rejectWithdrawalRequest(Long requestId, String reason) {
        WithdrawalRequest request = getRequestById(requestId);
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected.");
        }

        User teacher = request.getTeacher();
        // "Muzlatilgan" balansni hisobga qaytarish
        teacher.setPendingBalance(teacher.getPendingBalance().subtract(request.getAmount()));
        teacher.setAvailableBalance(teacher.getAvailableBalance().add(request.getAmount()));
        userRepository.save(teacher);

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setAdminComment(reason);
        request.setProcessedAt(LocalDateTime.now());
        requestRepository.save(request);
    }
    
    // --- Helper & Mapper Methods ---

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private WithdrawalRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Withdrawal request not found: " + id));
    }

    private WithdrawalRequestDto mapToDto(WithdrawalRequest request) {
        return WithdrawalRequestDto.builder()
                .id(request.getId())
                .teacherId(request.getTeacher().getId())
                .teacherName(request.getTeacher().getFirstName() + " " + request.getTeacher().getLastName())
                .amount(request.getAmount())
                .status(request.getStatus())
                .adminComment(request.getAdminComment())
                .requestedAt(request.getRequestedAt())
                .processedAt(request.getProcessedAt())
                .build();
    }
}
