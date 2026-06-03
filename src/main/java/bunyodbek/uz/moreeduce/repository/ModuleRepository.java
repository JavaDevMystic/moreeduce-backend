package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    @Query("SELECT m FROM Module m JOIN FETCH m.course WHERE m.id = :moduleId")
    Optional<Module> findByIdAndFetchCourse(Long moduleId);

    /**
     * Kursdagi modullarning eng katta tartib raqamini topadi.
     * Agar modul yo'q bo'lsa, 0 qaytaradi. COALESCE null qiymatni 0 ga o'giradi.
     * Bu "race condition" muammosini hal qiladi.
     */
    @Query("SELECT COALESCE(MAX(m.moduleOrder), 0) FROM Module m WHERE m.course.id = :courseId")
    int findMaxModuleOrder(Long courseId);
}
