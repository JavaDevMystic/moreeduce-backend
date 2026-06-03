package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Reflection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    @EntityGraph(attributePaths = {"criteria"})
    Optional<Reflection> findByLessonId(Long lessonId);
}
