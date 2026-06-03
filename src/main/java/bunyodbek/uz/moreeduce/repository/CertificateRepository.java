package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateCode(String certificateCode);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Certificate> findByStudentId(Long studentId);
}
