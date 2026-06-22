package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEnrollmentDto {

    private Long enrollmentId;

    private Long studentId;
    private String studentName;

    private Long courseId;
    private String courseName;

    private String paymentReceiptUrl;

    private EnrollmentStatus status;

    private LocalDateTime enrolledAt;
}
