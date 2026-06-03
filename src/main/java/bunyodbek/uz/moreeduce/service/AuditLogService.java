package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {

    /**
     * Audit log'larni dinamik parametlar bo'yicha qidiradi va paginatsiya bilan qaytaradi.
     *
     * @param userEmail Foydalanuvchi email'i (ixtiyoriy)
     * @param action Harakat turi (ixtiyoriy)
     * @param startDate Boshlanish sanasi (ixtiyoriy)
     * @param endDate Tugash sanasi (ixtiyoriy)
     * @param pageable Paginatsiya ma'lumotlari
     * @return AuditLogDto'lardan iborat Page
     */
    Page<AuditLogDto> searchLogs(String userEmail, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Belgilangan sanadan oldingi barcha audit yozuvlarini o'chiradi.
     *
     * @param beforeDate Shu sanadan oldingi log'lar o'chiriladi.
     */
    void deleteLogsBefore(LocalDateTime beforeDate);
}
