package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CreateAdminRequest;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.AuditLogRepository;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.SuperAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminServiceImpl implements SuperAdminService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserProfileDto createAdmin(CreateAdminRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already in use.");
        }
        User admin = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .isEmailVerified(true) // Adminlar uchun email tasdiqlash shart emas
                .build();
        return mapToDto(userRepository.save(admin));
    }

    @Override
    public List<UserProfileDto> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("User is not an admin.");
        }
        userRepository.deleteById(adminId);
    }

    @Override
    @Async // Bu metod asinxron ishlaydi
    @Transactional
    public void cleanupDatabase(LocalDateTime beforeDate) {
        log.info("Starting database cleanup process for entries before {}", beforeDate);
        
        // 1. Eski audit log'larni o'chirish
        log.info("Deleting old audit logs...");
        auditLogRepository.deleteLogsBefore(beforeDate);
        
        // 2. Tasdiqlanmagan va eski foydalanuvchilarni o'chirish
        log.info("Deleting old, unverified users...");
        userRepository.deleteByIsEmailVerifiedFalseAndCreatedAtBefore(beforeDate);
        
        // TODO: Boshqa keraksiz ma'lumotlarni (masalan, eski bildirishnomalar) tozalash logikasini qo'shish mumkin
        
        log.info("Database cleanup process finished.");
    }

    private UserProfileDto mapToDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
