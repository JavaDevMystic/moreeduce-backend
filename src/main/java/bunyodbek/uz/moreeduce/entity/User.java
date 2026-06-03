package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // O'qituvchilar uchun qo'shimcha maydonlar
    private boolean isVerified = false;
    private String resumeUrl;
    private String certificatesUrl;
    private String socialMediaLinks;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private String cardNumber;
    private int videoUploadLimit = Integer.MAX_VALUE;

    // Balans (faqat o'qituvchilar uchun)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    // Bloklash uchun
    private boolean isBlocked = false;

    // Email Verification
    private boolean isEmailVerified = false;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isBlocked && isEmailVerified;
    }
}
