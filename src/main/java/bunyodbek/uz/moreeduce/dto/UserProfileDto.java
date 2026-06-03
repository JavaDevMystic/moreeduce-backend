package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    // O'qituvchilar uchun qo'shimcha maydonlar
    private boolean isVerified;
    private boolean isBlocked; // Yangi maydon
    private String resumeUrl;
    private String certificatesUrl;
    private String socialMediaLinks;
    private String bio;
    
    private int profileCompletionPercentage; // Profil to'ldirilganlik foizi
}
