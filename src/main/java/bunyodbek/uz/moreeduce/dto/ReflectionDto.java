package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReflectionDto {
    private Long id;
    private String title;
    private Long lessonId;
    private String question1;
    private String question2;
    private String question3;
    private String question4;
    private List<ReflectionCriterionDto> criteria;
}
