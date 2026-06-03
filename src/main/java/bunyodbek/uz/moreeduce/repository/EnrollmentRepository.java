package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher"})
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    long countByCourseId(Long courseId);

    @Query("SELECT count(e) > 0 FROM Enrollment e WHERE e.student.id = :studentId AND e.course.teacher.id = :teacherId")
    boolean existsByTeacherIdAndStudentId(Long teacherId, Long studentId);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e WHERE e.course.teacher.id = :teacherId")
    long countDistinctStudentByTeacherId(Long teacherId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.teacher.id = :teacherId AND e.status = 'PENDING'")
    long countByTeacherIdAndStatusPending(Long teacherId);
}
