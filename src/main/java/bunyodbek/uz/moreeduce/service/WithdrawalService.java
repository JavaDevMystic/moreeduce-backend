package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.AdminEnrollmentDto;
import bunyodbek.uz.moreeduce.dto.WithdrawalRequestDto;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WithdrawalService {
    WithdrawalRequestDto createWithdrawalRequest(WithdrawalRequestDto requestDto, String teacherEmail);

    List<WithdrawalRequestDto> getAllWithdrawalRequests();

    List<WithdrawalRequestDto> getMyWithdrawalRequests(String teacherEmail);

    void approveWithdrawalRequest(Long requestId);

    void rejectWithdrawalRequest(Long requestId);

    Page<WithdrawalRequestDto> getAllWithdrawalRequests(WithdrawalStatus status, Pageable pageable);

    void rejectWithdrawalRequest(Long requestId, String reason);

    List<AdminEnrollmentDto> getPendingEnrollments();

    void updateEnrollmentStatus(
            Long enrollmentId,
            EnrollmentStatus status
    );
}
