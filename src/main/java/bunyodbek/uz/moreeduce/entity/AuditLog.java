package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; // Masalan: "COURSE_APPROVED", "USER_BLOCKED"
    private String description; // Batafsil ma'lumot
    private String performedBy; // Kim bajardi (email)

    @CreationTimestamp
    private LocalDateTime timestamp;
}
