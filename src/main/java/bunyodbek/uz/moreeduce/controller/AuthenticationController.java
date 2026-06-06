package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.service.AuthenticationService;
import bunyodbek.uz.moreeduce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Ro'yxatdan o'tish va kirish uchun API")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(
            summary = "Ro'yxatdan o'tish",
            description = "Yangi foydalanuvchi (Student yoki Teacher) ro'yxatdan o'tishi uchun",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli ro'yxatdan o'tildi",
                            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumotlar kiritildi")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @Operation(
            summary = "Tizimga kirish",
            description = "Email va parol orqali tizimga kirish",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli kirildi",
                            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Email yoki parol noto'g'ri")
            }
    )
    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(Principal principal) {
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @Operation(
            summary = "Tokenni yangilash",
            description = "Eski access token muddati tugaganda yangisini olish uchun",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Yangi token berildi",
                            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Refresh token yaroqsiz")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @Operation(
            summary = "Emailni tasdiqlash",
            description = "Ro'yxatdan o'tgandan so'ng emailga kelgan kodni tasdiqlash",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email muvaffaqiyatli tasdiqlandi"),
                    @ApiResponse(responseCode = "400", description = "Kod noto'g'ri yoki muddati o'tgan")
            }
    )
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        authenticationService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok("Email verified successfully");
    }

    @Operation(summary = "Health Check", description = "Server ishlayotganini tekshirish uchun")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(summary = "Test Email", description = "Email yuborishni tekshirish uchun")
    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            authenticationService.sendTestEmail(to);
            return ResponseEntity.ok("Email sent successfully to " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
}
