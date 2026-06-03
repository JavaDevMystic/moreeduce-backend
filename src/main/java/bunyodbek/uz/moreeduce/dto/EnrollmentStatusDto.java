package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrollmentStatusDto {
    private String status; // "ENROLLED" yoki "NOT_ENROLLED"
}
