package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.NotificationDto;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Bildirishnomalarni yuborish")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-to-all")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<String> sendToAllUsers(@RequestBody NotificationDto notificationDto) {
        notificationService.sendToAll(notificationDto);
        return ResponseEntity.ok("Notification sent to all users.");
    }

    @PostMapping("/send-to-role")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<String> sendToRole(
            @RequestParam Role role,
            @RequestBody NotificationDto notificationDto) {
        notificationService.sendToRole(role, notificationDto);
        return ResponseEntity.ok("Notification sent to all " + role.name() + "s.");
    }
}
