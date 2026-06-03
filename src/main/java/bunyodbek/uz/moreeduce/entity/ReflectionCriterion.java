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
@Table(name = "reflection_criteria")
public class ReflectionCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String criterion; // Mezon nomi

    @Column(nullable = false)
    private Integer points; // Shu mezon uchun maksimal ball

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reflection_id", nullable = false)
    private Reflection reflection;
}
