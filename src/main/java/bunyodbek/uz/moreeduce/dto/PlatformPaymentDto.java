package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformPaymentDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private BigDecimal amount;
    private String paymentReceiptUrl;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
}
