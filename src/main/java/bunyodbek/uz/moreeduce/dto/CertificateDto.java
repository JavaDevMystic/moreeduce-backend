package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateDto {
    private Long id;
    private String courseName;
    private String certificateCode;
    private LocalDateTime issueDate;
}
