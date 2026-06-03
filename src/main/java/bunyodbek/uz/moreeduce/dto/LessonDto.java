package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long id;
    private String title;
    private String videoUrl;
    private String videoQuality;
    private Long videoSize;
    private String transcription;
    private Integer lessonOrder;
    private Long courseId;
    private Long moduleId; // Dars qaysi modulga tegishli
    private List<String> fileUrls; // Qo'shimcha fayllar
    private Map<String, String> subtitles; // Har xil tillardagi subtitrlar uchun

    // Yangi maydonlar
    private boolean isCompleted;
    private boolean isLocked;
}
