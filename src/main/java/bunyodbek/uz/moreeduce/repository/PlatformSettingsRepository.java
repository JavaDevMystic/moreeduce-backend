package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.PlatformSettings;
import bunyodbek.uz.moreeduce.entity.SettingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {
    Optional<PlatformSettings> findBySettingKey(String settingKey);
    List<PlatformSettings> findByCategory(SettingCategory category);
}
