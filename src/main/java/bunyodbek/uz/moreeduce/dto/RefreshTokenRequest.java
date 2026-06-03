package bunyodbek.uz.moreeduce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token bo'sh bo'lmasligi kerak")
    private String refreshToken;
}
