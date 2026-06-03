package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByLessonId(Long lessonId);
    Optional<Quiz> findByModuleId(Long moduleId); // Yangi metod
}
