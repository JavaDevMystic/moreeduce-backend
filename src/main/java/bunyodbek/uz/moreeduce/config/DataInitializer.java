package bunyodbek.uz.moreeduce.config;

import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Agar Super Admin mavjud bo'lmasa, yaratamiz
        if (userRepository.findByEmail(superAdminEmail).isEmpty()) {
            User superAdmin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .role(Role.SUPER_ADMIN)
                    .isVerified(true) // Super Admin har doim tasdiqlangan
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Super Admin created: " + superAdminEmail);
        }
    }
}
