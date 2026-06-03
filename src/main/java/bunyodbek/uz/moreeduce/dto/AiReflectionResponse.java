package bunyodbek.uz.moreeduce.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiReflectionResponse {
    private List<CriterionScore> scores;
    private String generalFeedback;

    @Data
    public static class CriterionScore {
        private String criterion;
        private int score;
    }
}
