package site.addzero.processor

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.util.io.codegen.buildFilePath
import site.addzero.util.io.codegen.genCodeWithPackage
import java.io.File
import java.io.RandomAccessFile
import java.util.Base64

private const val SNAPSHOT_VERSION = "v1"

internal actual fun aggregateAndGenerateRoutes(
    sharedSourceDir: String,
    routeGenPkg: String,
    ownerModuleHint: String,
    sourceFilePaths: List<String>,
    routeItems: List<RouteRecord>,
    logger: KSPLogger,
) {
    if (sharedSourceDir.isBlank()) {
        logger.warn("sharedSourceDir 为空，跳过跨模块路由聚合")
        return
    }

    val aggregateRoot = File(
        sharedSourceDir,
        ".addzero/route-processor/${routeGenPkg.replace(".", "/")}"
    )
    val snapshotDir = File(aggregateRoot, "snapshots")
    val lockFile = File(aggregateRoot, "route-aggregate.lock")
    snapshotDir.mkdirs()

    RandomAccessFile(lockFile, "rw").use { lockHandle ->
        val fileLock = lockHandle.channel.lock()
        try {
            val moduleKey = resolveModuleKey(ownerModuleHint, sourceFilePaths)
            if (moduleKey == null) {
                logger.warn("无法确定当前模块标识，跳过跨模块路由聚合")
                return
            }

            val snapshotFile = File(snapshotDir, "$moduleKey.route-snapshot")
            if (routeItems.isEmpty()) {
                if (snapshotFile.exists()) {
                    snapshotFile.delete()
                }
            } else {
                writeSnapshot(snapshotFile, routeItems)
            }

            val mergedRoutes = readMergedRoutes(snapshotDir)
            warnDuplicateRoutePaths(mergedRoutes, logger)

            if (mergedRoutes.isEmpty()) {
                deleteAggregatedOutputs(sharedSourceDir, routeGenPkg)
                return
            }

            genCodeWithPackage(
                filePath = sharedSourceDir,
                pkg = routeGenPkg,
                filePrefix = "",
                fileName = ROUTE_KEYS_NAME,
                fileSuffix = ".kt",
                code = renderRouteKeysCode(mergedRoutes),
                skipExistFile = false,
            )

            genCodeWithPackage(
                filePath = sharedSourceDir,
                pkg = routeGenPkg,
                filePrefix = "",
                fileName = ROUTE_TABLE_NAME,
                fileSuffix = ".kt",
                code = renderRouteTableCode(mergedRoutes),
                skipExistFile = false,
            )
        } finally {
            fileLock.release()
        }
    }
}

private fun resolveModuleKey(ownerModuleHint: String, sourceFilePaths: List<String>): String? {
    if (ownerModuleHint.isNotBlank()) {
        return sanitizeModuleKey(ownerModuleHint)
    }

    val moduleRoots = sourceFilePaths
        .mapNotNull(::extractModuleRoot)
        .distinct()
        .sorted()
    if (moduleRoots.isEmpty()) {
        return null
    }

    val rootIdentity = moduleRoots.joinToString("|")
    val rootName = File(moduleRoots.first()).name.ifBlank { "module" }
    return sanitizeModuleKey("$rootName-${rootIdentity.hashCode().toUInt().toString(16)}")
}

private fun extractModuleRoot(sourceFilePath: String): String? {
    val normalizedPath = sourceFilePath.replace('\\', '/')
    val srcMarkerIndex = normalizedPath.indexOf("/src/")
    return if (srcMarkerIndex >= 0) {
        normalizedPath.substring(0, srcMarkerIndex)
    } else {
        File(normalizedPath).parentFile?.absolutePath
    }
}

private fun sanitizeModuleKey(value: String): String {
    return value
        .replace(':', '_')
        .replace('/', '_')
        .replace('\\', '_')
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .trim('_')
        .ifBlank { "module" }
}

private fun writeSnapshot(snapshotFile: File, routeItems: List<RouteRecord>) {
    val content = buildString {
        appendLine("# addzero-route-snapshot:$SNAPSHOT_VERSION")
        routeItems.forEach { route ->
            appendLine(route.encodeSnapshotLine())
        }
    }
    snapshotFile.parentFile?.mkdirs()
    snapshotFile.writeText(content)
}

private fun readMergedRoutes(snapshotDir: File): List<RouteRecord> {
    val mergedRoutes = linkedMapOf<String, RouteRecord>()
    snapshotDir.listFiles { file -> file.isFile && file.extension == "route-snapshot" }
        ?.sortedBy { it.name }
        ?.forEach { snapshotFile ->
            readSnapshot(snapshotFile).forEach { route ->
                mergedRoutes[route.uniqueId] = route
            }
        }
    return sortRoutes(mergedRoutes.values)
}

private fun readSnapshot(snapshotFile: File): List<RouteRecord> {
    return snapshotFile.readLines()
        .mapNotNull { line ->
            if (line.isBlank() || line.startsWith("#")) {
                null
            } else {
                decodeSnapshotLine(line)
            }
        }
}

private fun RouteRecord.encodeSnapshotLine(): String {
    return listOf(
        value,
        title,
        routePath,
        icon,
        order.toString(),
        qualifiedName,
        simpleName,
    ).joinToString("|") { encodeSnapshotField(it) }
}

private fun decodeSnapshotLine(line: String): RouteRecord? {
    val parts = line.split('|')
    if (parts.size != 7) {
        return null
    }

    return RouteRecord(
        value = decodeSnapshotField(parts[0]),
        title = decodeSnapshotField(parts[1]),
        routePath = decodeSnapshotField(parts[2]),
        icon = decodeSnapshotField(parts[3]),
        order = decodeSnapshotField(parts[4]).toDoubleOrNull() ?: 0.0,
        qualifiedName = decodeSnapshotField(parts[5]),
        simpleName = decodeSnapshotField(parts[6]),
    )
}

private fun encodeSnapshotField(value: String): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(Charsets.UTF_8))
}

private fun decodeSnapshotField(value: String): String {
    return String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
}

private fun warnDuplicateRoutePaths(routeItems: List<RouteRecord>, logger: KSPLogger) {
    routeItems.groupBy { it.routePath }
        .filterValues { it.size > 1 }
        .forEach { (routePath, duplicates) ->
            logger.warn(
                "检测到重复 routePath=$routePath，将按最后写入快照覆盖。冲突项: ${duplicates.joinToString { it.qualifiedName }}"
            )
        }
}

private fun deleteAggregatedOutputs(sharedSourceDir: String, routeGenPkg: String) {
    val routeKeysFile = File(
        buildFilePath(
            filePath = sharedSourceDir,
            pkg = routeGenPkg,
            filePrefix = "",
            fileName = ROUTE_KEYS_NAME,
            fileSuffix = ".kt",
        )
    )
    val routeTableFile = File(
        buildFilePath(
            filePath = sharedSourceDir,
            pkg = routeGenPkg,
            filePrefix = "",
            fileName = ROUTE_TABLE_NAME,
            fileSuffix = ".kt",
        )
    )
    if (routeKeysFile.exists()) {
        routeKeysFile.delete()
    }
    if (routeTableFile.exists()) {
        routeTableFile.delete()
    }
}
