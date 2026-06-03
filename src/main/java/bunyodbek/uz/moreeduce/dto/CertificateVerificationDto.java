package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateVerificationDto {
    private String studentName;
    private String courseName;
    private LocalDateTime issueDate;
    private boolean isValid;
}
