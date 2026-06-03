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
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT")
    private String submissionContent; // Talabaning javobi (matn yoki link)

    private String fileUrl; // Yuklangan fayl manzili

    private Integer grade; // Baho (masalan, 0 dan 100 gacha)

    @Column(columnDefinition = "TEXT")
    private String teacherFeedback; // O'qituvchi izohi

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING; // Holati: PENDING, GRADED, RESUBMIT

    @CreationTimestamp
    private LocalDateTime submittedAt;
}
