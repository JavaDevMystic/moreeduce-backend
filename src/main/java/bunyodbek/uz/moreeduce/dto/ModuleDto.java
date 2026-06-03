package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private Long id;
    private String title;
    private Long courseId;
    private int moduleOrder;
    private Integer totalScore;
    private Integer passingScore;
    private List<LessonDto> lessons;
}
