package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_payments")
public class PlatformPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    private String paymentReceiptUrl; // To'lov cheki rasmi

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status = EnrollmentStatus.PENDING; // To'lov statusi

    @CreationTimestamp
    private LocalDateTime createdAt;
}
