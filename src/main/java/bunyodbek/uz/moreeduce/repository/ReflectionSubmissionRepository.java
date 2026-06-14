package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.ReflectionSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReflectionSubmissionRepository extends JpaRepository<ReflectionSubmission, Long> {
    
    @EntityGraph(attributePaths = {"student", "reflection", "reflection.lesson"})
    List<ReflectionSubmission> findByStudentId(Long studentId);

    List<ReflectionSubmission> findByReflectionId(Long reflectionId);


    @EntityGraph(attributePaths = {"student", "reflection", "reflection.lesson", "criterionResults", "criterionResults.criterion"})
    Optional<ReflectionSubmission> findFirstByReflectionIdAndStudentIdOrderBySubmittedAtDesc(Long reflectionId, Long studentId);

    @EntityGraph(attributePaths = {"student", "reflection", "reflection.lesson", "criterionResults", "criterionResults.criterion"})
    Optional<ReflectionSubmission> findFirstByReflectionLessonIdAndStudentIdOrderBySubmittedAtDesc(@Param("lessonId") Long lessonId, @Param("studentId") Long studentId);

    @EntityGraph(attributePaths = {"reflection", "reflection.criteria"})
    @Query("SELECT rs FROM ReflectionSubmission rs WHERE rs.id = :id")
    Optional<ReflectionSubmission> findByIdWithReflectionAndCriteria(@Param("id") Long id);

    @Query("SELECT rs FROM ReflectionSubmission rs " +
           "JOIN rs.reflection r " +
           "JOIN r.lesson l " +
           "JOIN l.course c " +
           "WHERE c.id = :courseId")
    List<ReflectionSubmission> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COALESCE(rs.reflectionIndex, 0) FROM ReflectionSubmission rs " +
           "JOIN rs.reflection r " +
           "JOIN r.lesson l " +
           "JOIN l.course c " +
           "WHERE c.id = :courseId")
    List<Double> findScoresByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT AVG(COALESCE(rs.reflectionIndex, 0)) FROM ReflectionSubmission rs " +
           "JOIN rs.reflection r " +
           "JOIN r.lesson l " +
           "JOIN l.course c " +
           "WHERE c.id = :courseId " +
           "GROUP BY rs.student.id")
    List<Double> findAverageScoresPerStudentByCourseId(@Param("courseId") Long courseId);
}
