package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults
import site.addzero.kbox.core.support.walkRegularFiles
import java.io.File

@Single
class KboxLargeFileService(
    private val pathService: KboxPathService,
) {
    fun scan(
        settings: KboxSettings,
    ): List<KboxLargeFileCandidate> {
        val normalized = KboxDefaults.normalize(settings)
        val threshold = normalized.largeFileThresholdBytes
        val roots = normalized.largeFileScanRoots.map(::File)
        val results = linkedMapOf<String, KboxLargeFileCandidate>()
        walkRegularFiles(roots) { file ->
            if (file.length() < threshold) {
                return@walkRegularFiles
            }
            val relativePath = pathService.offloadRelativePath(file)
            results[file.absolutePath] = KboxLargeFileCandidate(
                sourcePath = file.absolutePath,
                fileName = file.name,
                sizeBytes = file.length(),
                lastModifiedMillis = file.lastModified(),
                remoteRelativePath = relativePath,
            )
        }
        return results.values.sortedByDescending { it.sizeBytes }
    }
}
