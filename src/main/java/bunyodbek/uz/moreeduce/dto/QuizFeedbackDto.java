package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizFeedbackDto {
    private Long id;
    private Integer minScore;
    private Integer maxScore;
    private String title;
    private String feedback;
}
