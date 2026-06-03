package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAssessmentDto {
    private Double score; // 0-10 ball
    private String feedback; // Izoh
}
