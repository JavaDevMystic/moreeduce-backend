package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.SettingCategory;
import bunyodbek.uz.moreeduce.entity.SettingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingsDto {
    private Long id;
    private String settingKey;
    private String settingValue;
    private SettingType type;
    private SettingCategory category;
    private String description;
}
