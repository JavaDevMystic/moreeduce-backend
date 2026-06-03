package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionSubmissionDto {
    private Long id;
    private Long studentId;
    private Long reflectionId;
    
    // Javoblar
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;

    // Baholar
    private Double selfScore;
    private Double teacherScore;
    private Double reflectionIndex;
    private Integer aiTotalScore;
    
    // Mezonlar bo'yicha AI baholari
    private List<ReflectionCriterionResultDto> criterionResults;
    
    private String generalAiFeedback;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
}
