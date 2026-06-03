package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    Optional<StudentProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    boolean existsByStudentIdAndLessonIdAndIsCompletedTrue(Long studentId, Long lessonId);

    @Query("SELECT count(sp) FROM StudentProgress sp WHERE sp.student.id = :studentId AND sp.lesson.course.id = :courseId AND sp.isCompleted = true")
    long countByStudentIdAndCourseIdAndIsCompletedTrue(Long studentId, Long courseId);

    // CourseServiceImpl uchun qo'shilgan yangi metod
    List<StudentProgress> findByStudentIdAndLesson_CourseId(Long studentId, Long courseId);
}
