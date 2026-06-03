package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.WithdrawalRequest;
import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long>, JpaSpecificationExecutor<WithdrawalRequest> {
    
    @EntityGraph(attributePaths = {"teacher"})
    List<WithdrawalRequest> findByTeacherId(Long teacherId);
    
    long countByStatus(WithdrawalStatus status);

    @Override
    @EntityGraph(attributePaths = {"teacher"})
    Page<WithdrawalRequest> findAll(Pageable pageable);
}
