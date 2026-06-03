package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogDto {
    private Long id;
    private String action;
    private String description;
    private String performedBy;
    private LocalDateTime timestamp;
}
