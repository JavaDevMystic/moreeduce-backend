package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByLessonId(Long lessonId);
}
