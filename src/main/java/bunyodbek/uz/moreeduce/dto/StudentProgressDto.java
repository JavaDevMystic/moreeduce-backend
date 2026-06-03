package bunyodbek.uz.moreeduce.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudentProgressDto {
    private Long studentId;
    private String studentName;
    private Double averageScore; // Umumiy o'rtacha ball
    
    // Faollik statistikasi
    private Long totalActivityCount;
    private String lastActiveAt;
    
    // Vaqt bo'yicha o'zgarish (Sana -> Ball)
    private Map<String, Double> progressHistory; 
    
    // Modullar kesimida natijalar
    private List<ModuleResultDto> moduleResults;
}
