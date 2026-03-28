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
    routeOwnerModuleDir: String,
    moduleSourceRoots: List<String>,
    routeItems: List<RouteRecord>,
    logger: KSPLogger,
) {
    if (sharedSourceDir.isBlank()) {
        logger.warn("sharedSourceDir 为空，跳过跨模块路由聚合")
        return
    }

    val aggregateRoot = resolveAggregateRoot(
        sharedSourceDir = sharedSourceDir,
        routeGenPkg = routeGenPkg,
    )
    val snapshotDir = File(aggregateRoot, "snapshots")
    val lockFile = File(aggregateRoot, "route-aggregate.lock")
    snapshotDir.mkdirs()

    RandomAccessFile(lockFile, "rw").use { lockHandle ->
        val fileLock = lockHandle.channel.lock()
        try {
            val moduleKey = resolveModuleKey(
                moduleSourceRoots = moduleSourceRoots,
                routeItems = routeItems,
            )
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
                deleteAggregatedOutputs(
                    sharedSourceDir = sharedSourceDir,
                    routeGenPkg = routeGenPkg,
                    routeOwnerModuleDir = routeOwnerModuleDir,
                )
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

            val routeTableOutputDir = normalizeRouteOwnerModuleDir(routeOwnerModuleDir, logger)
                ?: return
            deleteLegacySharedRouteTable(
                sharedSourceDir = sharedSourceDir,
                routeGenPkg = routeGenPkg,
                routeTableOutputDir = routeTableOutputDir,
            )
            genCodeWithPackage(
                filePath = routeTableOutputDir,
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

private fun resolveModuleKey(
    moduleSourceRoots: List<String>,
    routeItems: List<RouteRecord>,
): String? {
    resolveModuleKeyFromRouteItems(routeItems)?.let { return it }

    val moduleRoots = moduleSourceRoots
        .distinct()
        .sorted()
    if (moduleRoots.isEmpty()) {
        return null
    }

    val rootIdentity = moduleRoots.joinToString("|")
    val rootName = File(moduleRoots.first()).name.ifBlank { "module" }
    return sanitizeModuleKey("$rootName-${rootIdentity.hashCode().toUInt().toString(16)}")
}

private fun resolveModuleKeyFromRouteItems(
    routeItems: List<RouteRecord>,
): String? {
    val packageSegments = routeItems.mapNotNull { route ->
        route.qualifiedName
            .substringBeforeLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.split('.')
    }
    if (packageSegments.isEmpty()) {
        return null
    }

    val sharedPrefix = packageSegments.reduce { acc, segments ->
        acc.zip(segments)
            .takeWhile { (left, right) -> left == right }
            .map { (value, _) -> value }
    }
    if (sharedPrefix.isEmpty()) {
        return null
    }

    val packageName = sharedPrefix.joinToString(".")
    val moduleName = sharedPrefix.lastOrNull { it != "screen" }
        ?.ifBlank { "module" }
        ?: sharedPrefix.last().ifBlank { "module" }
    return sanitizeModuleKey("$moduleName-${packageName.hashCode().toUInt().toString(16)}")
}

private fun resolveAggregateRoot(
    sharedSourceDir: String,
    routeGenPkg: String,
): File {
    val sharedSourcePath = File(sharedSourceDir).absoluteFile
        .invariantSeparatorsPath
    val moduleRoot = sharedSourcePath.substringBefore("/src/")
        .takeIf { it != sharedSourcePath && it.isNotBlank() }
        ?: File(sharedSourceDir).absoluteFile
            .parentFile
            ?.parentFile
            ?.parentFile
            ?.absolutePath
        ?: sharedSourceDir
    return File(
        moduleRoot,
        "build/addzero/route-processor/${routeGenPkg.replace(".", "/")}"
    )
}

private fun normalizeRouteOwnerModuleDir(
    routeOwnerModuleDir: String,
    logger: KSPLogger,
): String? {
    val normalized = routeOwnerModuleDir.trim()
    if (normalized.isBlank()) {
        logger.warn("routeOwnerModule 为空，已跳过 RouteTable 生成")
        return null
    }

    if (!File(normalized).isAbsolute) {
        logger.warn("routeOwnerModule 必须是绝对源码目录，当前值=$normalized，已跳过 RouteTable 生成")
        return null
    }

    return normalized
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

private fun deleteAggregatedOutputs(
    sharedSourceDir: String,
    routeGenPkg: String,
    routeOwnerModuleDir: String,
) {
    val routeKeysFile = generatedOutputFile(sharedSourceDir, routeGenPkg, ROUTE_KEYS_NAME)
    val legacyRouteTableFile = generatedOutputFile(sharedSourceDir, routeGenPkg, ROUTE_TABLE_NAME)
    val routeTableFile = routeOwnerModuleDir
        .takeIf { it.isNotBlank() && File(it).isAbsolute }
        ?.let { generatedOutputFile(it, routeGenPkg, ROUTE_TABLE_NAME) }

    if (routeKeysFile.exists()) {
        routeKeysFile.delete()
    }
    if (legacyRouteTableFile.exists()) {
        legacyRouteTableFile.delete()
    }
    if (routeTableFile?.exists() == true) {
        routeTableFile.delete()
    }
}

private fun deleteLegacySharedRouteTable(
    sharedSourceDir: String,
    routeGenPkg: String,
    routeTableOutputDir: String,
) {
    if (routeTableOutputDir == sharedSourceDir) {
        return
    }

    val legacyRouteTableFile = generatedOutputFile(sharedSourceDir, routeGenPkg, ROUTE_TABLE_NAME)
    if (legacyRouteTableFile.exists()) {
        legacyRouteTableFile.delete()
    }
}

private fun generatedOutputFile(baseDir: String, routeGenPkg: String, fileName: String): File {
    return File(
        buildFilePath(
            filePath = baseDir,
            pkg = routeGenPkg,
            filePrefix = "",
            fileName = fileName,
            fileSuffix = ".kt",
        )
    )
}
