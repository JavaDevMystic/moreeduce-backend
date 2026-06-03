package bunyodbek.uz.moreeduce.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseUpdateByAdminDto {
    private String title;
    private String description;
    private BigDecimal price;
    private boolean isPublic;
}
