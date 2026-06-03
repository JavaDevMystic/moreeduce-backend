package bunyodbek.uz.moreeduce.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizSubmissionDto {
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private Long selectedOptionId;
    }
}
