package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reflections")
public class Reflection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(columnDefinition = "TEXT")
    private String question1;
    @Column(columnDefinition = "TEXT")
    private String question2;
    @Column(columnDefinition = "TEXT")
    private String question3;
    @Column(columnDefinition = "TEXT")
    private String question4;

    @OneToMany(mappedBy = "reflection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReflectionCriterion> criteria;
}
