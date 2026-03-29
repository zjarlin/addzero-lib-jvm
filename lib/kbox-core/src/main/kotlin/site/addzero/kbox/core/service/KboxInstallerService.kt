package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxInstallerArchiveRecord
import site.addzero.kbox.core.model.KboxInstallerCandidate
import site.addzero.kbox.core.model.KboxInstallerCollectResult
import site.addzero.kbox.core.model.KboxInstallerDeleteResult
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults
import site.addzero.kbox.core.support.deleteEmptyParentDirectories
import site.addzero.kbox.core.support.moveFileReplacing
import site.addzero.kbox.core.support.walkRegularFiles
import java.io.File

@Single
class KboxInstallerService(
    private val pathService: KboxPathService,
    private val historyStore: KboxHistoryStore,
) {
    fun scan(
        settings: KboxSettings,
    ): List<KboxInstallerCandidate> {
        val normalized = KboxDefaults.normalize(settings)
        val rulesByExtension = normalized.installerRules.associateBy { it.extension.lowercase() }
        val results = linkedMapOf<String, KboxInstallerCandidate>()
        val roots = normalized.installerScanRoots.map(::File)
        walkRegularFiles(roots) { file ->
            val extension = file.extension.lowercase()
            val rule = rulesByExtension[extension] ?: return@walkRegularFiles
            val target = pathService.packageTarget(rule, file)
            val candidate = KboxInstallerCandidate(
                sourcePath = file.absolutePath,
                fileName = file.name,
                sizeBytes = file.length(),
                lastModifiedMillis = file.lastModified(),
                platform = rule.platform,
                extension = extension,
                bucket = rule.bucket,
                destinationRelativePath = pathService.appDataDir()
                    .toPath()
                    .relativize(target.toPath())
                    .toString()
                    .replace(File.separatorChar, '/'),
                destinationAbsolutePath = target.absolutePath,
            )
            results[candidate.sourcePath] = candidate
        }
        return results.values
            .sortedWith(
                compareByDescending<KboxInstallerCandidate> { it.lastModifiedMillis }
                    .thenByDescending { it.sizeBytes },
            )
    }

    fun collect(
        candidates: List<KboxInstallerCandidate>,
    ): KboxInstallerCollectResult {
        val archived = mutableListOf<KboxInstallerArchiveRecord>()
        val skipped = mutableListOf<String>()
        candidates.forEach { candidate ->
            val sourceFile = File(candidate.sourcePath)
            val destinationFile = File(candidate.destinationAbsolutePath)
            if (!sourceFile.isFile) {
                skipped += "源文件不存在：${candidate.sourcePath}"
                return@forEach
            }
            if (sourceFile.canonicalPath == destinationFile.canonicalPath) {
                skipped += "文件已在目标目录：${candidate.sourcePath}"
                return@forEach
            }
            moveFileReplacing(sourceFile, destinationFile)
            deleteEmptyParentDirectories(
                startDirectory = sourceFile.parentFile,
                stopDirectories = emptySet(),
            )
            archived += KboxInstallerArchiveRecord(
                sourcePath = candidate.sourcePath,
                destinationPath = destinationFile.absolutePath,
                destinationRelativePath = candidate.destinationRelativePath,
                sizeBytes = candidate.sizeBytes,
                platform = candidate.platform,
                extension = candidate.extension,
                archivedAtMillis = System.currentTimeMillis(),
            )
        }
        historyStore.appendInstallerHistory(archived)
        return KboxInstallerCollectResult(
            archived = archived,
            skipped = skipped,
        )
    }

    fun deleteFiles(
        sourcePaths: List<String>,
    ): KboxInstallerDeleteResult {
        val deleted = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        sourcePaths.distinct().forEach { sourcePath ->
            val file = File(sourcePath)
            if (!file.isFile) {
                skipped += "文件不存在：$sourcePath"
                return@forEach
            }
            if (!file.delete()) {
                skipped += "删除失败：$sourcePath"
                return@forEach
            }
            deleteEmptyParentDirectories(
                startDirectory = file.parentFile,
                stopDirectories = emptySet(),
            )
            deleted += sourcePath
        }
        return KboxInstallerDeleteResult(
            deleted = deleted,
            skipped = skipped,
        )
    }
}
