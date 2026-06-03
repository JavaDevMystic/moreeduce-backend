package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.PlatformSettingsDto;
import bunyodbek.uz.moreeduce.dto.UpdateSettingRequest;
import bunyodbek.uz.moreeduce.entity.SettingCategory;
import bunyodbek.uz.moreeduce.service.PlatformSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings", description = "Platforma sozlamalari (Admin)")
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
public class PlatformSettingsController {

    private final PlatformSettingsService settingsService;

    @Operation(summary = "Barcha sozlamalarni olish yoki kategoriya bo'yicha filtrlash")
    @GetMapping
    public ResponseEntity<List<PlatformSettingsDto>> getSettings(
            @RequestParam(required = false) SettingCategory category) {
        return ResponseEntity.ok(settingsService.getSettings(category));
    }

    @Operation(summary = "Sozlamani kaliti bo'yicha olish")
    @GetMapping("/{key}")
    public ResponseEntity<PlatformSettingsDto> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(settingsService.getSettingByKey(key));
    }

    @Operation(summary = "Sozlamani qiymatini o'zgartirish")
    @PutMapping("/{key}")
    public ResponseEntity<PlatformSettingsDto> updateSetting(
            @PathVariable String key,
            @RequestBody UpdateSettingRequest request) {
        return ResponseEntity.ok(settingsService.updateSetting(key, request.getValue()));
    }
}
