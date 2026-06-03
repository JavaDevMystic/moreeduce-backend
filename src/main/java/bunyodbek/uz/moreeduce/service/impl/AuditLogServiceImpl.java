package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.AuditLogDto;
import bunyodbek.uz.moreeduce.entity.AuditLog;
import bunyodbek.uz.moreeduce.repository.AuditLogRepository;
import bunyodbek.uz.moreeduce.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLogDto> searchLogs(String userEmail, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // Specification yordamida dinamik so'rov yaratish
        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userEmail != null && !userEmail.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("performedBy")), "%" + userEmail.toLowerCase() + "%"));
            }
            if (action != null && !action.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> auditLogPage = auditLogRepository.findAll(spec, pageable);
        return auditLogPage.map(this::mapToDto);
    }

    @Override
    @Transactional
    public void deleteLogsBefore(LocalDateTime beforeDate) {
        auditLogRepository.deleteLogsBefore(beforeDate);
    }

    private AuditLogDto mapToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .description(log.getDescription())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getTimestamp())
                .build();
    }
}
