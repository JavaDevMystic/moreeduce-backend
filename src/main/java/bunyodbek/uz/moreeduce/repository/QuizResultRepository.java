package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.QuizResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    @EntityGraph(attributePaths = {"quiz", "student"})
    Page<QuizResult> findByQuizId(Long quizId, Pageable pageable);

    @EntityGraph(attributePaths = {"quiz"})
    List<QuizResult> findByStudentId(Long studentId);

    @Query("SELECT qr FROM QuizResult qr JOIN qr.quiz q JOIN q.lesson l WHERE qr.student.id = :studentId AND l.id = :lessonId ORDER BY qr.submittedAt DESC")
    List<QuizResult> findByStudentIdAndLessonId(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    @Query("SELECT l.module.id, SUM(qr.earnedPoints) FROM QuizResult qr JOIN qr.quiz q JOIN q.lesson l WHERE qr.student.id = :studentId AND l.course.id = :courseId GROUP BY l.module.id")
    List<Object[]> findTotalScoresByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
