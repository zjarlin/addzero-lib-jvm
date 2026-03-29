package site.addzero.kbox.core.support

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest

internal fun walkRegularFiles(
    roots: List<File>,
    visit: (File) -> Unit,
) {
    val visitedRoots = linkedSetOf<String>()
    roots.asSequence()
        .filter { it.exists() }
        .forEach { root ->
            val canonicalRoot = root.canonicalFile
            if (!visitedRoots.add(canonicalRoot.absolutePath)) {
                return@forEach
            }
            Files.walkFileTree(
                canonicalRoot.toPath(),
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        if (attrs.isRegularFile) {
                            visit(file.toFile())
                        }
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFileFailed(
                        file: Path,
                        exc: java.io.IOException,
                    ): FileVisitResult {
                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
}

internal fun moveFileReplacing(
    source: File,
    target: File,
) {
    target.parentFile?.mkdirs()
    runCatching {
        Files.move(
            source.toPath(),
            target.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING,
        )
    }.recoverCatching {
        Files.copy(
            source.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.delete(source.toPath())
    }.getOrThrow()
}

internal fun stableShortHash(
    value: String,
): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
        .take(10)
}

internal fun sanitizeFileName(
    name: String,
): String {
    return name.replace(Regex("[^A-Za-z0-9._-]+"), "_")
        .trim('_')
        .ifBlank { "file" }
}

internal fun normalizeSegments(
    path: String,
): List<String> {
    val normalized = path.replace('\\', '/')
    if (normalized.matches(Regex("^[A-Za-z]:/.*"))) {
        val drive = normalized.substring(0, 1).uppercase()
        return listOf("WindowsDrive-$drive") + normalized
            .substring(3)
            .split('/')
            .filter { it.isNotBlank() }
    }
    return normalized.split('/')
        .filter { it.isNotBlank() }
}

internal fun deleteEmptyParentDirectories(
    startDirectory: File?,
    stopDirectories: Set<String>,
) {
    var current = startDirectory?.absoluteFile
    while (current != null && current.exists()) {
        if (stopDirectories.contains(current.absolutePath)) {
            break
        }
        val children = current.listFiles().orEmpty()
        if (children.isNotEmpty()) {
            break
        }
        if (!current.delete()) {
            break
        }
        current = current.parentFile?.absoluteFile
    }
}
