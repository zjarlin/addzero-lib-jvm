package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults

@Single
class KboxSettingsRepository(
    private val json: Json,
    private val pathService: KboxPathService,
    private val migrationService: KboxAppDataMigrationService,
) {
    fun load(): KboxSettings {
        val settingsFile = pathService.settingsFile()
        if (!settingsFile.isFile) {
            val defaults = KboxDefaults.defaultSettings().copy(
                localAppDataOverride = pathService.currentAppDataOverride(),
            )
            return save(defaults)
        }
        val decoded = json.decodeFromString<KboxSettings>(settingsFile.readText())
        return KboxDefaults.normalize(decoded)
    }

    fun save(
        settings: KboxSettings,
    ): KboxSettings {
        val normalized = KboxDefaults.normalize(settings)
        val currentOverride = pathService.currentAppDataOverride()
        val targetOverride = normalized.localAppDataOverride
        if (currentOverride != targetOverride) {
            migrationService.migrate(targetOverride)
        }
        val settingsFile = pathService.settingsFileForOverride(targetOverride)
        settingsFile.parentFile?.mkdirs()
        settingsFile.writeText(json.encodeToString(normalized))
        pathService.writeAppDataOverride(targetOverride)
        return normalized
    }
}
