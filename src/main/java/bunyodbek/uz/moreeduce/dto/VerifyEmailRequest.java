package bunyodbek.uz.moreeduce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {
    @NotBlank(message = "Email bo'sh bo'lmasligi kerak")
    @Email(message = "Email noto'g'ri formatda")
    private String email;

    @NotBlank(message = "Tasdiqlash kodi bo'sh bo'lmasligi kerak")
    private String code;
}
