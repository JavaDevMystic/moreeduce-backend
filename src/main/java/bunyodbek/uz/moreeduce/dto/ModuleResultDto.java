package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleResultDto {
    private String moduleTitle;
    private Double quizScore;
    private Double reflectionScore;
}
