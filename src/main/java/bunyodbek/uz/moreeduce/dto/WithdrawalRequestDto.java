package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
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
public class WithdrawalRequestDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private BigDecimal amount;
    private WithdrawalStatus status;
    private String adminComment; // Yangi maydon
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}
