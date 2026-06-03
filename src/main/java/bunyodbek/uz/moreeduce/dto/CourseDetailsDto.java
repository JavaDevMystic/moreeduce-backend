package bunyodbek.uz.moreeduce.dto;

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
public class CourseDetailsDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String introVideoUrl;
    private BigDecimal price;
    private String category;
    private String language;
    private Double rating;
    
    private Long teacherId;
    private String teacherName;
    private String teacherBio;
    private String teacherAvatarUrl; // Agar bo'lsa

    private List<ModuleDto> modules; // Sillabus
    private List<CommentDto> reviews; // Izohlar
}
