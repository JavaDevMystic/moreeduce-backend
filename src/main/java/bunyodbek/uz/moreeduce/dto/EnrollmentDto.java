package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    private Long id;
    private Long studentId;
    private String studentName; // XATO TUZATILDI: Yetishmayotgan maydon qo'shildi
    private Long courseId;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private String status;
}
