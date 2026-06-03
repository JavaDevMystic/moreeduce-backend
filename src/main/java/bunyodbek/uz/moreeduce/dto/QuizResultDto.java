package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private String studentName;
    private int totalPossiblePoints;
    private int earnedPoints;
    private double scorePercentage;
    private LocalDateTime submittedAt;
    private String feedbackTitle; // Yangi maydon
    private String feedbackMessage; // Yangi maydon
}
