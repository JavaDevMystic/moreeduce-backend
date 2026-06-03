package bunyodbek.uz.moreeduce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizDto {
    private Long id;
    private String title;
    private boolean shuffleQuestions;
    private Long lessonId;
    private Long moduleId;
    private List<QuestionDto> questions;
    private List<QuizFeedbackDto> feedbacks; // Yangi maydon
}
