package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_settings")
public class PlatformSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String settingKey; // Kalit (masalan, "TEACHER_COMMISSION_RATE")

    @Column(columnDefinition = "TEXT")
    private String settingValue; // Qiymat (masalan, "15.5")

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettingType type; // Qiymat turi (NUMBER, STRING, BOOLEAN, JSON)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettingCategory category; // Sozlama guruhi (GENERAL, PAYMENT, etc.)

    private String description; // Admin panelida ko'rsatish uchun tavsif
}
