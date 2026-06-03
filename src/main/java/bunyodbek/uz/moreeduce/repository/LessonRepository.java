package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    int countByModuleId(Long moduleId);

    long countByCourseId(Long courseId); // Yangi metod qo'shildi

    @Modifying
    @Query("UPDATE Lesson l SET l.lessonOrder = :newOrder WHERE l.id = :lessonId")
    void updateLessonOrder(Long lessonId, int newOrder);
}
