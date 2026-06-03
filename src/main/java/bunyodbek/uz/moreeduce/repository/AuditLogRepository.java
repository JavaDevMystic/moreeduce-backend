package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    long countByPerformedBy(String email);
    
    Optional<AuditLog> findFirstByPerformedByOrderByTimestampDesc(String email);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByPerformedBy(String performedBy);

    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :beforeDate")
    void deleteLogsBefore(LocalDateTime beforeDate);
}
