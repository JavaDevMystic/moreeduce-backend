package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reflection_submissions")
public class ReflectionSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reflection_id", nullable = false)
    private Reflection reflection;

    // Talaba javoblari
    @Column(columnDefinition = "TEXT")
    private String answer1;
    @Column(columnDefinition = "TEXT")
    private String answer2;
    @Column(columnDefinition = "TEXT")
    private String answer3;
    @Column(columnDefinition = "TEXT")
    private String answer4;

    // Gibrid Baholash
    private Double selfScore; // O'zini baholash (0-10)
    private Double teacherScore; // O'qituvchi bahosi (0-10)

    private Double reflectionIndex; // Avtomatik hisoblanadigan indeks

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReflectionCriterionResult> criterionResults;

    @Column(columnDefinition = "TEXT")
    private String generalAiFeedback; // Umumiy AI izohi

    /**
     * Refleksiya Indeksini (RI) joriy status va mavjud baholarga qarab hisoblaydi va o'rnatadi.
     */
    public void calculateAndSetReflectionIndex() {
        double sb = Objects.requireNonNullElse(selfScore, 0.0);
        double tb = Objects.requireNonNullElse(teacherScore, 0.0);
        
        double aiTotalScore = 0.0;
        if (criterionResults != null && !criterionResults.isEmpty()) {
            aiTotalScore = criterionResults.stream().mapToInt(ReflectionCriterionResult::getScore).sum();
        }

        if (teacherScore != null) {
            this.reflectionIndex = (sb + aiTotalScore + tb) / 3.0; // Bu yerda vaznlar o'zgarishi mumkin
            this.status = SubmissionStatus.GRADED;
        } else if (aiTotalScore > 0) {
            this.reflectionIndex = (sb + aiTotalScore) / 2.0;
            this.status = SubmissionStatus.GRADED;
        } else {
            this.reflectionIndex = 0.0;
            this.status = SubmissionStatus.PENDING;
        }
    }
}
