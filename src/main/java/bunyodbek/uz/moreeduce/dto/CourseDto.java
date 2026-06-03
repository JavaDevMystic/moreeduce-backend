package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl; // Rasm uchun URL
    private BigDecimal price; // Kurs narxi
    private String category;
    private String language;
    private Double rating;
    private CourseStatus status;
    private boolean isPublic; // Public/Private
    private Long teacherId;
    private String teacherName;
    private List<ModuleDto> modules; // Kurs modullari
    private long studentsCount; // Kursga yozilgan talabalar soni
}
