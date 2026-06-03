package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfileUpdateDto {
    private String bio;
    private String resumeUrl;
    private String certificatesUrl;
    private String socialMediaLinks;
}
