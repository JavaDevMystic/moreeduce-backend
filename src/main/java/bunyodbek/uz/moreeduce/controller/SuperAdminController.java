package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.CreateAdminRequest;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;
import bunyodbek.uz.moreeduce.service.PlatformSettingsService;
import bunyodbek.uz.moreeduce.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/superadmin")
@RequiredArgsConstructor
@Tag(name = "Super Admin Management", description = "Super Admin uchun boshqaruv paneli")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final PlatformSettingsService settingsService;

    @Operation(summary = "Yangi Admin yaratish")
    @PostMapping("/admins")
    public ResponseEntity<UserProfileDto> createAdmin(@RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(superAdminService.createAdmin(request));
    }

    @Operation(summary = "Barcha Adminlarni olish")
    @GetMapping("/admins")
    public ResponseEntity<List<UserProfileDto>> getAllAdmins() {
        return ResponseEntity.ok(superAdminService.getAllAdmins());
    }

    @Operation(summary = "Adminni o'chirish")
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long adminId) {
        superAdminService.deleteAdmin(adminId);
        return ResponseEntity.ok("Admin deleted successfully");
    }

    @Operation(summary = "Platformani 'Texnik Xizmat' rejimiga o'tkazish")
    @PostMapping("/maintenance/enable")
    public ResponseEntity<String> enableMaintenanceMode() {
        settingsService.updateSetting("MAINTENANCE_MODE", "true");
        return ResponseEntity.ok("Maintenance mode enabled. Only Super Admins can access the platform.");
    }

    @Operation(summary = "Platformani 'Texnik Xizmat' rejimidan chiqarish")
    @PostMapping("/maintenance/disable")
    public ResponseEntity<String> disableMaintenanceMode() {
        settingsService.updateSetting("MAINTENANCE_MODE", "false");
        return ResponseEntity.ok("Maintenance mode disabled. The platform is now live for all users.");
    }

    @Operation(summary = "Ma'lumotlar bazasini keraksiz ma'lumotlardan tozalash")
    @PostMapping("/database/cleanup")
    public ResponseEntity<String> cleanupDatabase(
            @Parameter(description = "Shu sanadan oldingi yozuvlar o'chiriladi (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        superAdminService.cleanupDatabase(beforeDate);
        return ResponseEntity.accepted().body("Database cleanup process started in the background.");
    }
}
