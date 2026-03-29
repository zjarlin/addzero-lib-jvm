package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxDotfileCandidate
import site.addzero.kbox.core.model.KboxDotfileEntry
import site.addzero.kbox.core.model.KboxDotfileStatus
import site.addzero.kbox.core.model.KboxManagedFileKind
import site.addzero.kbox.core.support.normalizeSegments
import site.addzero.kbox.core.support.sanitizeFileName
import site.addzero.kbox.core.support.stableShortHash
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Single
class KboxDotfileService(
    private val json: Json,
    private val pathService: KboxPathService,
) {
    fun discoverCandidates(): List<KboxDotfileCandidate> {
        val managedEntries = listEntries().associateBy { entry -> entry.targetPath }
        val results = linkedMapOf<String, KboxDotfileCandidate>()
        commonCandidatePaths().forEach { candidate ->
            if (candidate.exists() || Files.isSymbolicLink(candidate.toPath()) || managedEntries.containsKey(candidate.absolutePath)) {
                results[candidate.absolutePath] = candidateView(
                    entry = managedEntries[candidate.absolutePath],
                    targetFile = candidate,
                )
            }
        }
        managedEntries.values.forEach { entry ->
            results.putIfAbsent(
                entry.targetPath,
                candidateView(
                    entry = entry,
                    targetFile = File(entry.targetPath),
                ),
            )
        }
        return results.values.sortedBy { candidate -> candidate.logicalName.lowercase() }
    }

    fun listEntries(): List<KboxDotfileEntry> {
        val registryFile = pathService.dotfileRegistryFile()
        if (!registryFile.isFile) {
            return emptyList()
        }
        return json.decodeFromString<List<KboxDotfileEntry>>(registryFile.readText())
            .sortedBy { entry -> entry.logicalName.lowercase() }
    }

    fun importTarget(
        targetPath: String,
        logicalName: String = "",
    ): KboxDotfileCandidate {
        val targetFile = normalizeTargetFile(targetPath)
        val sourceFile = resolveSourceFile(targetFile)
        require(sourceFile.exists()) {
            "Dotfile 不存在：${sourceFile.absolutePath}"
        }
        val localType = sourceFile.toManagedFileKind()
        val entry = KboxDotfileEntry(
            logicalName = logicalName.trim().ifBlank { inferLogicalName(targetFile) },
            targetPath = targetFile.absolutePath,
            localType = localType,
            canonicalRelativePath = canonicalRelativePath(
                targetFile = targetFile,
                logicalName = logicalName.trim().ifBlank { inferLogicalName(targetFile) },
                localType = localType,
            ),
            enabled = true,
        )
        val canonicalFile = canonicalFile(entry)
        copyPath(sourceFile, canonicalFile)
        ensureManagedLink(
            targetFile = targetFile,
            canonicalFile = canonicalFile,
        )
        saveEntries(
            listEntries()
                .filterNot { saved -> saved.targetPath == entry.targetPath }
                .plus(entry)
                .sortedBy { saved -> saved.logicalName.lowercase() },
        )
        return candidateView(entry, targetFile)
    }

    fun rebuildLink(
        targetPath: String,
    ): KboxDotfileCandidate {
        val entry = listEntries().firstOrNull { saved -> saved.targetPath == normalizeTargetFile(targetPath).absolutePath }
            ?: error("未找到已托管的 dotfile：$targetPath")
        val canonicalFile = canonicalFile(entry)
        require(canonicalFile.exists()) {
            "Canonical 源不存在：${canonicalFile.absolutePath}"
        }
        ensureManagedLink(
            targetFile = File(entry.targetPath),
            canonicalFile = canonicalFile,
        )
        return candidateView(entry, File(entry.targetPath))
    }

    fun removeManagedTarget(
        targetPath: String,
    ) {
        val normalizedTarget = normalizeTargetFile(targetPath).absolutePath
        val entry = listEntries().firstOrNull { saved -> saved.targetPath == normalizedTarget }
            ?: error("未找到已托管的 dotfile：$targetPath")
        val targetFile = File(entry.targetPath)
        val canonicalFile = canonicalFile(entry)
        if (canonicalFile.exists()) {
            replaceTargetWithCopy(
                targetFile = targetFile,
                canonicalFile = canonicalFile,
                kind = entry.localType,
            )
            canonicalFile.deleteRecursively()
        } else if (Files.isSymbolicLink(targetFile.toPath())) {
            Files.deleteIfExists(targetFile.toPath())
        }
        saveEntries(
            listEntries().filterNot { saved -> saved.targetPath == normalizedTarget },
        )
    }

    private fun candidateView(
        entry: KboxDotfileEntry?,
        targetFile: File,
    ): KboxDotfileCandidate {
        val resolvedEntry = entry
        val canonicalFile = resolvedEntry?.let(::canonicalFile)
        val localType = resolvedEntry?.localType ?: resolveSourceFile(targetFile).toManagedFileKind()
        val status = when {
            resolvedEntry == null -> KboxDotfileStatus.CANDIDATE
            !resolvedEntry.enabled -> KboxDotfileStatus.DISABLED
            canonicalFile == null || !canonicalFile.exists() -> KboxDotfileStatus.MISSING
            !targetFile.exists() && !Files.isSymbolicLink(targetFile.toPath()) -> KboxDotfileStatus.MISSING
            Files.isSymbolicLink(targetFile.toPath()) -> {
                val linkedPath = runCatching {
                    Files.readSymbolicLink(targetFile.toPath()).resolvedAgainst(targetFile.parentFile.toPath())
                }.getOrNull()
                val canonicalPath = canonicalFile.toPath().normalizedAbsolutePath()
                if (linkedPath == canonicalPath) {
                    KboxDotfileStatus.MANAGED
                } else {
                    KboxDotfileStatus.DRIFTED
                }
            }

            else -> KboxDotfileStatus.CONFLICT
        }
        val message = when (status) {
            KboxDotfileStatus.CANDIDATE -> "可导入"
            KboxDotfileStatus.MANAGED -> "已托管"
            KboxDotfileStatus.DRIFTED -> "链接已漂移"
            KboxDotfileStatus.CONFLICT -> "目标位置存在非链接内容"
            KboxDotfileStatus.MISSING -> "目标或 canonical 内容缺失"
            KboxDotfileStatus.DISABLED -> "已停用"
        }
        return KboxDotfileCandidate(
            logicalName = resolvedEntry?.logicalName ?: inferLogicalName(targetFile),
            targetPath = targetFile.absolutePath,
            localType = localType,
            canonicalRelativePath = resolvedEntry?.canonicalRelativePath.orEmpty(),
            canonicalPath = canonicalFile?.absolutePath.orEmpty(),
            status = status,
            managed = resolvedEntry != null,
            message = message,
        )
    }

    private fun ensureManagedLink(
        targetFile: File,
        canonicalFile: File,
    ) {
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists() || Files.isSymbolicLink(targetFile.toPath())) {
            val existingLink = runCatching { Files.readSymbolicLink(targetFile.toPath()) }.getOrNull()
            if (existingLink?.resolvedAgainst(targetFile.parentFile.toPath()) == canonicalFile.toPath().normalizedAbsolutePath()) {
                return
            }
            backupTarget(targetFile)
            deleteTarget(targetFile)
        }
        Files.createSymbolicLink(
            targetFile.toPath(),
            canonicalFile.toPath().normalizedAbsolutePath(),
        )
    }

    private fun replaceTargetWithCopy(
        targetFile: File,
        canonicalFile: File,
        kind: KboxManagedFileKind,
    ) {
        deleteTarget(targetFile)
        targetFile.parentFile?.mkdirs()
        if (kind == KboxManagedFileKind.DIRECTORY) {
            canonicalFile.copyRecursively(targetFile, overwrite = true)
        } else {
            targetFile.parentFile?.mkdirs()
            Files.copy(
                canonicalFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun backupTarget(
        targetFile: File,
    ) {
        if (!targetFile.exists() && !Files.isSymbolicLink(targetFile.toPath())) {
            return
        }
        val backupDir = File(
            pathService.dotfilesBackupDir(),
            "${System.currentTimeMillis()}-${sanitizeFileName(targetFile.name)}",
        )
        if (Files.isSymbolicLink(targetFile.toPath())) {
            val linkTarget = Files.readSymbolicLink(targetFile.toPath())
            backupDir.parentFile?.mkdirs()
            backupDir.writeText(linkTarget.toString())
            return
        }
        copyPath(targetFile, backupDir)
    }

    private fun copyPath(
        source: File,
        target: File,
    ) {
        target.parentFile?.mkdirs()
        if (target.exists()) {
            target.deleteRecursively()
        }
        if (source.isDirectory) {
            source.copyRecursively(target, overwrite = true)
        } else {
            Files.copy(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun deleteTarget(
        targetFile: File,
    ) {
        if (Files.isSymbolicLink(targetFile.toPath())) {
            Files.deleteIfExists(targetFile.toPath())
            return
        }
        if (targetFile.exists()) {
            check(targetFile.deleteRecursively()) {
                "删除目标失败：${targetFile.absolutePath}"
            }
        }
    }

    private fun canonicalFile(
        entry: KboxDotfileEntry,
    ): File {
        return File(pathService.dotfilesDir(), entry.canonicalRelativePath)
    }

    private fun saveEntries(
        entries: List<KboxDotfileEntry>,
    ) {
        val registryFile = pathService.dotfileRegistryFile()
        registryFile.parentFile?.mkdirs()
        registryFile.writeText(json.encodeToString(entries))
    }

    private fun commonCandidatePaths(): List<File> {
        val userHome = File(System.getProperty("user.home").orEmpty())
        return listOf(
            ".zshrc",
            ".bashrc",
            ".bash_profile",
            ".gitconfig",
            ".ssh/config",
            ".config/git",
            ".config/gh",
            ".config/nvim",
            ".config/fish",
            ".config/kitty",
            ".config/alacritty",
            ".config/wezterm",
            ".config/starship.toml",
        ).map { relativePath -> File(userHome, relativePath) }
    }

    private fun resolveSourceFile(
        targetFile: File,
    ): File {
        if (!Files.isSymbolicLink(targetFile.toPath())) {
            return targetFile
        }
        val linkTarget = Files.readSymbolicLink(targetFile.toPath())
        return if (linkTarget.isAbsolute) {
            linkTarget.toFile()
        } else {
            targetFile.parentFile.resolve(linkTarget.toString())
        }
    }

    private fun normalizeTargetFile(
        targetPath: String,
    ): File {
        val trimmed = targetPath.trim()
        require(trimmed.isNotBlank()) {
            "Dotfile 路径不能为空"
        }
        val expanded = if (trimmed == "~") {
            System.getProperty("user.home").orEmpty()
        } else if (trimmed.startsWith("~/")) {
            File(System.getProperty("user.home").orEmpty(), trimmed.removePrefix("~/")).path
        } else {
            trimmed
        }
        return File(expanded).absoluteFile
    }

    private fun inferLogicalName(
        targetFile: File,
    ): String {
        val userHome = File(System.getProperty("user.home").orEmpty()).absolutePath
        return targetFile.absolutePath.removePrefix(userHome).trimStart(File.separatorChar).ifBlank { targetFile.name }
    }

    private fun canonicalRelativePath(
        targetFile: File,
        logicalName: String,
        localType: KboxManagedFileKind,
    ): String {
        val normalizedName = sanitizeFileName(logicalName)
        val suffix = stableShortHash(targetFile.absolutePath)
        val extension = if (localType == KboxManagedFileKind.FILE) {
            targetFile.extension.lowercase().takeIf { it.isNotBlank() }?.let { ".$it" }.orEmpty()
        } else {
            ""
        }
        return listOf("managed")
            .plus(normalizeSegments(targetFile.absolutePath).takeLast(2))
            .joinToString(
                separator = "/",
                postfix = "/${normalizedName}__${suffix}$extension",
            )
    }

    private fun Path.normalizedAbsolutePath(): Path {
        return if (isAbsolute) {
            normalize()
        } else {
            toAbsolutePath().normalize()
        }
    }

    private fun Path.resolvedAgainst(
        parentPath: Path,
    ): Path {
        return if (isAbsolute) {
            normalize()
        } else {
            parentPath.resolve(this).normalize()
        }
    }

    private fun File.toManagedFileKind(): KboxManagedFileKind {
        return if (isDirectory) {
            KboxManagedFileKind.DIRECTORY
        } else {
            KboxManagedFileKind.FILE
        }
    }
}
