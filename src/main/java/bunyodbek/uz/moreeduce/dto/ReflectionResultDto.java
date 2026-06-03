package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReflectionResultDto {
    private Long id;
    private Long reflectionId;
    private Long studentId;
    
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    
    private Double selfScore;
    private Double aiScore;
    private Double teacherScore;
    private Double reflectionIndex;
    
    private String aiFeedback;
    private SubmissionStatus status; // PENDING, GRADED
    private LocalDateTime submittedAt;
}
