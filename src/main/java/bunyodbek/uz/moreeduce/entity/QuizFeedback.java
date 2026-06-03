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
@Table(name = "quiz_feedbacks")
public class QuizFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer minScore; // Oraliqning minimal balli (shu ball ham kiradi)

    @Column(nullable = false)
    private Integer maxScore; // Oraliqning maksimal balli (shu ball ham kiradi)

    @Column(nullable = false)
    private String title; // Natija sarlavhasi (masalan, "Yuqori daraja")

    @Lob
    @Column(nullable = false)
    private String feedback; // Xabar matni

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
