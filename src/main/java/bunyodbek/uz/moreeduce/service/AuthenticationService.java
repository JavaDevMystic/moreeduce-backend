package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.JwtAuthenticationResponse;
import bunyodbek.uz.moreeduce.dto.RefreshTokenRequest;
import bunyodbek.uz.moreeduce.dto.SignInRequest;
import bunyodbek.uz.moreeduce.dto.SignUpRequest;

public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request);
    JwtAuthenticationResponse signin(SignInRequest request);
    JwtAuthenticationResponse refreshToken(RefreshTokenRequest request);
    void verifyEmail(String email, String code);
    void sendTestEmail(String to); // Yangi metod
}
