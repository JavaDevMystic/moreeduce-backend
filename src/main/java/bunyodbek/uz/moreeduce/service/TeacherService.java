package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.PlatformPaymentDto;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface TeacherService {
    // Student to'lovini tasdiqlash
    void approveEnrollment(Long enrollmentId, EnrollmentStatus status, String teacherEmail);

    // Platformaga to'lov qilish (Chek bilan)
    PlatformPaymentDto payPlatformFee(BigDecimal amount, MultipartFile receiptFile, String teacherEmail) throws IOException;

    // Platforma to'lovlari tarixini ko'rish
    List<PlatformPaymentDto> getMyPlatformPayments(String teacherEmail);
}
