package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.AuditLogDto;
import bunyodbek.uz.moreeduce.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Tizim harakatlari jurnali (Admin)")
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Loglarni qidirish, filtrlash va paginatsiya qilish",
               description = "Barcha parametlar ixtiyoriy. Hech qanday parametr berilmasa, barcha log'lar paginatsiya bilan qaytariladi.")
    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> searchLogs(
            @Parameter(description = "Foydalanuvchi email'i bo'yicha filtrlash")
            @RequestParam(required = false) String userEmail,
            
            @Parameter(description = "Harakat turi bo'yicha filtrlash (masalan, USER_LOGIN)")
            @RequestParam(required = false) String action,
            
            @Parameter(description = "Boshlanish sanasi (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "Tugash sanasi (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            @Parameter(hidden = true) // Swagger'da standart Pageable parametrlarini ko'rsatmaslik uchun
            Pageable pageable) {
        
        Page<AuditLogDto> logs = auditLogService.searchLogs(userEmail, action, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(summary = "Eski loglarni o'chirish (Faqat SUPER_ADMIN)",
               description = "Belgilangan sanadan oldingi barcha audit yozuvlarini o'chiradi.")
    @DeleteMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteLogsBefore(
            @Parameter(description = "Shu sanadan oldingi log'lar o'chiriladi (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        
        auditLogService.deleteLogsBefore(beforeDate);
        return ResponseEntity.noContent().build();
    }
}
