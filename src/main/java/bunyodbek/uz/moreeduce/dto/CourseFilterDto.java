package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFilterDto {
    private String query; // Qidiruv so'zi (search -> query ga o'zgartirildi)
    private String category;
    private String language;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
