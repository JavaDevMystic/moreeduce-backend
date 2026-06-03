package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.PlatformSettingsDto;
import bunyodbek.uz.moreeduce.entity.SettingCategory;

import java.util.List;

public interface PlatformSettingsService {

    List<PlatformSettingsDto> getSettings(SettingCategory category);

    PlatformSettingsDto getSettingByKey(String key);

    PlatformSettingsDto updateSetting(String key, String value);
}
