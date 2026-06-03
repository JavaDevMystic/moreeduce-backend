package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken; // "token" nomini "accessToken" ga o'zgartirdim
    private String refreshToken;
}
