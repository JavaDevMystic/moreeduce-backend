package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.PlatformPaymentDto;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.EnrollmentRepository;
import bunyodbek.uz.moreeduce.repository.PlatformPaymentRepository;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.EmailService;
import bunyodbek.uz.moreeduce.service.FileStorageService;
import bunyodbek.uz.moreeduce.service.TeacherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PlatformPaymentRepository platformPaymentRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void approveEnrollment(Long enrollmentId, EnrollmentStatus status, String teacherEmail) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        enrollment.setStatus(status);
        enrollmentRepository.save(enrollment);

        // Studentga bildirishnoma yuborish
        String studentEmail = enrollment.getStudent().getEmail();
        String subject = "Enrollment Status Update";
        String message = "Your enrollment for course " + enrollment.getCourse().getTitle() + 
                         " has been " + status.name().toLowerCase() + ".";
        emailService.sendNotification(studentEmail, subject, message);
    }

    @Override
    @Transactional
    public PlatformPaymentDto payPlatformFee(BigDecimal amount, MultipartFile receiptFile, String teacherEmail) throws IOException {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        String receiptUrl = fileStorageService.uploadFile(receiptFile);

        PlatformPayment payment = PlatformPayment.builder()
                .teacher(teacher)
                .amount(amount)
                .paymentReceiptUrl(receiptUrl)
                .status(EnrollmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        PlatformPayment savedPayment = platformPaymentRepository.save(payment);
        return mapToDto(savedPayment);
    }

    @Override
    public List<PlatformPaymentDto> getMyPlatformPayments(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        return platformPaymentRepository.findByTeacherId(teacher.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PlatformPaymentDto mapToDto(PlatformPayment payment) {
        return PlatformPaymentDto.builder()
                .id(payment.getId())
                .teacherId(payment.getTeacher().getId())
                .teacherName(payment.getTeacher().getFirstName() + " " + payment.getTeacher().getLastName())
                .amount(payment.getAmount())
                .paymentReceiptUrl(payment.getPaymentReceiptUrl())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
