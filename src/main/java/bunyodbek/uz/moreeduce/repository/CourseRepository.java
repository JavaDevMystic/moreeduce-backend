package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Course;
import bunyodbek.uz.moreeduce.entity.CourseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    
    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByTeacherId(Long teacherId);

    @Override
    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findAll();

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByStatus(CourseStatus status);

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.modules m LEFT JOIN FETCH m.lessons WHERE c.id = :courseId")
    Optional<Course> findByIdWithModulesAndLessons(Long courseId);

    @Query("SELECT c FROM Course c ORDER BY size(c.enrollments) DESC")
    List<Course> findTop5ByOrderByEnrollmentsDesc(Pageable pageable);

    @Query("""
            SELECT c
            FROM Course c
            LEFT JOIN c.enrollments e
            WHERE c.teacher.id = :teacherId
            GROUP BY c
            ORDER BY COUNT(e) DESC
            """)
    List<Course> findMostPopularCourses(
            @Param("teacherId") Long teacherId,
            Pageable pageable
    );

    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findByCategory(String category);
    List<Course> findByLanguage(String language);
    List<Course> findByRatingGreaterThanEqual(Double rating);

    long countByStatus(CourseStatus status);
}
