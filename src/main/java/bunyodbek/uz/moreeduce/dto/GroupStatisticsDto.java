package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupStatisticsDto {
    private Long courseId;
    private int totalStudents;
    private Double meanScore; // O'rtacha ball (M)
    private Double standardDeviation; // Standart og'ish (σ)
    private Double minScore;
    private Double maxScore;
}
