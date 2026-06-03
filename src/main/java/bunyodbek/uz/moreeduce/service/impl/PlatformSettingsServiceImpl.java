package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.PlatformSettingsDto;
import bunyodbek.uz.moreeduce.entity.AuditLog;
import bunyodbek.uz.moreeduce.entity.PlatformSettings;
import bunyodbek.uz.moreeduce.entity.SettingCategory;
import bunyodbek.uz.moreeduce.entity.SettingType;
import bunyodbek.uz.moreeduce.repository.AuditLogRepository;
import bunyodbek.uz.moreeduce.repository.PlatformSettingsRepository;
import bunyodbek.uz.moreeduce.service.PlatformSettingsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    public static final String SETTINGS_CACHE = "settings";

    private final PlatformSettingsRepository settingsRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<PlatformSettingsDto> getSettings(SettingCategory category) {
        List<PlatformSettings> settings;
        if (category != null) {
            settings = settingsRepository.findByCategory(category);
        } else {
            settings = settingsRepository.findAll();
        }
        return settings.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = SETTINGS_CACHE, key = "#key")
    public PlatformSettingsDto getSettingByKey(String key) {
        log.info("Fetching setting '{}' from database.", key);
        return settingsRepository.findBySettingKey(key)
                .map(this::mapToDto)
                .orElseGet(() -> {
                    log.warn("Setting '{}' not found in database. Returning default value.", key);
                    // Agar MAINTENANCE_MODE topilmasa, standart "false" qiymatini qaytaramiz
                    if ("MAINTENANCE_MODE".equals(key)) {
                        return PlatformSettingsDto.builder()
                                .settingKey(key)
                                .settingValue("false")
                                .type(SettingType.BOOLEAN)
                                .build();
                    }
                    // Boshqa sozlamalar uchun xatolik saqlanib qoladi
                    throw new EntityNotFoundException("Setting not found: " + key);
                });
    }

    @Override
    @Transactional
    @CacheEvict(value = SETTINGS_CACHE, key = "#key") // Sozlama o'zgarganda keshni tozalash
    public PlatformSettingsDto updateSetting(String key, String value) {
        PlatformSettings setting = settingsRepository.findBySettingKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Setting not found: " + key));

        validateSettingValue(setting.getType(), value);

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(value);
        PlatformSettings updatedSetting = settingsRepository.save(setting);

        // Audit log yozish
        createAuditLog(key, oldValue, value);
        
        log.info("Setting '{}' updated successfully.", key);
        return mapToDto(updatedSetting);
    }

    private void validateSettingValue(SettingType type, String value) {
        try {
            switch (type) {
                case NUMBER:
                    Double.parseDouble(value);
                    break;
                case BOOLEAN:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException("Value must be 'true' or 'false'");
                    }
                    break;
                case JSON:
                    objectMapper.readTree(value);
                    break;
                case STRING:
                    // String uchun maxsus validatsiya shart emas
                    break;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for NUMBER type setting: " + value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format for JSON type setting.");
        }
    }

    private void createAuditLog(String key, String oldValue, String newValue) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        AuditLog logEntry = AuditLog.builder()
                .action("SETTING_UPDATE")
                .description(String.format("Setting '%s' changed from '%s' to '%s'", key, oldValue, newValue))
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(logEntry);
    }

    private PlatformSettingsDto mapToDto(PlatformSettings setting) {
        return PlatformSettingsDto.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .type(setting.getType())
                .category(setting.getCategory())
                .description(setting.getDescription())
                .build();
    }
}
