package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxInstallerArchiveRecord
import site.addzero.kbox.core.model.KboxOffloadRecord
import java.io.File

@Single
class KboxHistoryStore(
    private val json: Json,
    private val pathService: KboxPathService,
) {
    fun readInstallerHistory(): List<KboxInstallerArchiveRecord> {
        return readList(pathService.installerHistoryFile())
    }

    fun appendInstallerHistory(
        records: List<KboxInstallerArchiveRecord>,
    ) {
        if (records.isEmpty()) {
            return
        }
        writeList(
            file = pathService.installerHistoryFile(),
            values = readInstallerHistory() + records,
        )
    }

    fun readOffloadHistory(): List<KboxOffloadRecord> {
        return readList(pathService.offloadHistoryFile())
    }

    fun appendOffloadHistory(
        records: List<KboxOffloadRecord>,
    ) {
        if (records.isEmpty()) {
            return
        }
        writeList(
            file = pathService.offloadHistoryFile(),
            values = readOffloadHistory() + records,
        )
    }

    private inline fun <reified T> readList(
        file: File,
    ): List<T> {
        if (!file.isFile) {
            return emptyList()
        }
        return json.decodeFromString(file.readText())
    }

    private inline fun <reified T> writeList(
        file: File,
        values: List<T>,
    ) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(values))
    }
}
