package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;
    private String resumeUrl;
    private String certificatesUrl;
    private String socialMediaLinks;
    private int coursesCount; // Kurslar soni
    private int studentsCount; // Jami talabalar soni
}
