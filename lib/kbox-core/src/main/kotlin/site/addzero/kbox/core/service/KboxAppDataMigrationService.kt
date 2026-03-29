package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxAppDataMigrationPlan
import site.addzero.kbox.core.model.KboxAppDataMigrationResult
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Single
class KboxAppDataMigrationService(
    private val pathService: KboxPathService,
) {
    fun preview(
        localAppDataOverride: String,
    ): KboxAppDataMigrationPlan {
        val currentDir = pathService.appDataDir().absoluteFile
        val targetDir = pathService.resolveAppDataDir(
            localAppDataOverride = localAppDataOverride,
            createDirectories = false,
        ).absoluteFile
        val warnings = mutableListOf<String>()
        val blockers = mutableListOf<String>()
        val needsMigration = currentDir.absolutePath != targetDir.absolutePath

        if (!needsMigration) {
            warnings += "目标目录与当前目录一致，无需迁移。"
        }
        if (currentDir.toPath().startsWith(targetDir.toPath()) || targetDir.toPath().startsWith(currentDir.toPath())) {
            blockers += "目标目录不能与当前目录互为父子目录。"
        }
        if (needsMigration && targetDir.exists() && targetDir.listFiles().orEmpty().isNotEmpty()) {
            blockers += "目标目录已存在内容，请先清空或更换目录。"
        }
        if (!targetDir.parentFile.exists()) {
            warnings += "目标目录的父目录不存在，保存时会自动创建。"
        }
        return KboxAppDataMigrationPlan(
            currentPath = currentDir.absolutePath,
            targetPath = targetDir.absolutePath,
            needsMigration = needsMigration,
            blockers = blockers,
            warnings = warnings,
        )
    }

    fun migrate(
        localAppDataOverride: String,
    ): KboxAppDataMigrationResult {
        val plan = preview(localAppDataOverride)
        check(plan.canMigrate) {
            plan.blockers.joinToString(separator = "\n")
        }
        val sourceDir = File(plan.currentPath)
        val targetDir = File(plan.targetPath)
        if (!plan.needsMigration) {
            targetDir.mkdirs()
            return KboxAppDataMigrationResult(
                previousPath = sourceDir.absolutePath,
                currentPath = targetDir.absolutePath,
                migrated = false,
            )
        }
        if (!sourceDir.exists() || sourceDir.listFiles().isNullOrEmpty()) {
            targetDir.mkdirs()
            return KboxAppDataMigrationResult(
                previousPath = sourceDir.absolutePath,
                currentPath = targetDir.absolutePath,
                migrated = false,
            )
        }

        val stagingDir = File(
            sourceDir.parentFile,
            "${sourceDir.name}.migration-${System.currentTimeMillis()}",
        )
        check(!stagingDir.exists()) {
            "迁移暂存目录已存在：${stagingDir.absolutePath}"
        }
        moveDirectory(sourceDir, stagingDir)
        try {
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            moveDirectory(stagingDir, targetDir)
        } catch (error: Throwable) {
            targetDir.deleteRecursively()
            moveDirectory(stagingDir, sourceDir)
            throw IllegalStateException(
                "数据目录迁移失败：${error.message.orEmpty()}",
                error,
            )
        }
        return KboxAppDataMigrationResult(
            previousPath = sourceDir.absolutePath,
            currentPath = targetDir.absolutePath,
            migrated = true,
        )
    }

    private fun moveDirectory(
        sourceDir: File,
        targetDir: File,
    ) {
        targetDir.parentFile?.mkdirs()
        runCatching {
            Files.move(
                sourceDir.toPath(),
                targetDir.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
            )
        }.recoverCatching {
            sourceDir.copyRecursively(targetDir, overwrite = true)
            check(sourceDir.deleteRecursively()) {
                "迁移后清理旧目录失败：${sourceDir.absolutePath}"
            }
        }.getOrThrow()
    }
}
