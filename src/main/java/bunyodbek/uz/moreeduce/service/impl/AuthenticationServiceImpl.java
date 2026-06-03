package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.JwtAuthenticationResponse;
import bunyodbek.uz.moreeduce.dto.RefreshTokenRequest;
import bunyodbek.uz.moreeduce.dto.SignInRequest;
import bunyodbek.uz.moreeduce.dto.SignUpRequest;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.Token;
import bunyodbek.uz.moreeduce.entity.TokenType;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.TokenRepository;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.AuthenticationService;
import bunyodbek.uz.moreeduce.service.EmailService;
import bunyodbek.uz.moreeduce.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request) {
        if (request.getRole() != Role.STUDENT && request.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("You can only register as STUDENT or TEACHER");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        String verificationCode = RandomStringUtils.randomNumeric(6);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isEmailVerified(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        
        log.info("User signed up: {}. Verification email sent.", user.getEmail());
        
        return JwtAuthenticationResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .build();
    }

    @Override
    public JwtAuthenticationResponse signin(SignInRequest request) {
        log.info("Attempting to sign in user: {}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        
        log.info("User authenticated: {}", user.getEmail());

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        log.info("Generated tokens for user: {}. AccessToken length: {}, RefreshToken length: {}", 
                 user.getEmail(), accessToken.length(), refreshToken.length());

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String userEmail = jwtService.extractUserName(request.getRefreshToken());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (jwtService.isTokenValid(request.getRefreshToken(), user)) {
            var accessToken = jwtService.generateAccessToken(user);
            var refreshToken = jwtService.generateRefreshToken(user); // Yangi refresh token (Rotation)
            
            revokeAllUserTokens(user);
            saveUserToken(user, accessToken);
            
            return JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }

    @Override
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification code expired");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", email);
    }

    @Override
    public void sendTestEmail(String to) {
        emailService.sendNotification(to, "Test Email", "This is a test email from MoreEduce.");
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
