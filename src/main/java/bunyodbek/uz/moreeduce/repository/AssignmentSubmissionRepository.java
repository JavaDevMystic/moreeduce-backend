package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.lesson.course.teacher.id = :teacherId AND s.status = 'PENDING'")
    long countByTeacherIdAndStatusPending(Long teacherId);
}
