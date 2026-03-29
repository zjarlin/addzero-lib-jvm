package site.addzero.processor

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.util.io.codegen.buildFilePath
import site.addzero.util.io.codegen.genCodeWithPackage
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

private const val SNAPSHOT_VERSION = "v2"
private const val FILE_LOCK_RETRY_COUNT = 20
private const val FILE_LOCK_RETRY_DELAY_MS = 50L
private val aggregateLocks = ConcurrentHashMap<String, Any>()

internal actual fun aggregateAndGenerateRoutes(
    sharedSourceDir: String,
    routeGenPkg: String,
    routeOwnerModuleDir: String,
    moduleKeyHint: String,
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

    withAggregateLock(lockFile, logger) {
        val moduleKey = resolveModuleKey(
            moduleKeyHint = moduleKeyHint,
            moduleSourceRoots = moduleSourceRoots,
            routeItems = routeItems,
        )
        if (moduleKey == null) {
            if (routeItems.isNotEmpty() || moduleSourceRoots.isNotEmpty()) {
                logger.warn("无法确定当前模块标识，跳过跨模块路由聚合")
            }
            return@withAggregateLock
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
        validateMergedRoutes(mergedRoutes, logger)
        warnDuplicateRoutePaths(mergedRoutes, logger)

        if (mergedRoutes.isEmpty()) {
            deleteAggregatedOutputs(
                sharedSourceDir = sharedSourceDir,
                routeGenPkg = routeGenPkg,
                routeOwnerModuleDir = routeOwnerModuleDir,
            )
            return@withAggregateLock
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
            ?: return@withAggregateLock
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
    }
}

private inline fun withAggregateLock(
    lockFile: File,
    logger: KSPLogger,
    action: () -> Unit,
) {
    val lockKey = lockFile.absoluteFile.normalize().path
    val monitor = aggregateLocks.computeIfAbsent(lockKey) { Any() }
    synchronized(monitor) {
        RandomAccessFile(lockFile, "rw").use { lockHandle ->
            val fileLock = acquireFileLock(lockHandle.channel, lockFile, logger)
            try {
                action()
            } finally {
                fileLock.release()
            }
        }
    }
}

private fun resolveModuleKey(
    moduleKeyHint: String,
    moduleSourceRoots: List<String>,
    routeItems: List<RouteRecord>,
): String? {
    moduleKeyHint
        .trim()
        .takeIf { it.isNotBlank() }
        ?.let(::sanitizeModuleKey)
        ?.let { return it }

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

private fun acquireFileLock(
    channel: FileChannel,
    lockFile: File,
    logger: KSPLogger,
): FileLock {
    repeat(FILE_LOCK_RETRY_COUNT) { attempt ->
        try {
            channel.tryLock()?.let { return it }
        } catch (_: OverlappingFileLockException) {
            if (attempt == 0) {
                logger.warn("检测到并发路由聚合，正在重试获取锁: ${lockFile.absolutePath}")
            }
        }
        Thread.sleep(FILE_LOCK_RETRY_DELAY_MS)
    }

    return try {
        channel.lock()
    } catch (exception: OverlappingFileLockException) {
        throw IllegalStateException(
            "无法获取路由聚合锁: ${lockFile.absolutePath}",
            exception,
        )
    }
}

internal fun validateMergedRoutes(
    routeItems: List<RouteRecord>,
    logger: KSPLogger,
) {
    val validationErrors = buildList {
        routeItems
            .filter { route -> route.sceneId.isNotBlank() }
            .groupBy { route -> route.sceneId }
            .forEach { (sceneId, routes) ->
                val reference = routes.first()
                routes.drop(1).forEach { route ->
                    if (
                        route.sceneName != reference.sceneName ||
                        route.sceneIcon != reference.sceneIcon ||
                        route.sceneOrder != reference.sceneOrder
                    ) {
                        add(
                            "scene.id=$sceneId metadata mismatch: ${reference.qualifiedName} " +
                                "declares (${reference.sceneName}, ${reference.sceneIcon}, ${reference.sceneOrder}) " +
                                "but ${route.qualifiedName} declares (${route.sceneName}, ${route.sceneIcon}, ${route.sceneOrder})"
                        )
                    }
                }

                val defaultRoutes = routes.filter { route -> route.defaultInScene }
                if (defaultRoutes.size > 1) {
                    add(
                        "scene.id=$sceneId has multiple default routes: " +
                            defaultRoutes.joinToString { route -> route.qualifiedName }
                    )
                }
            }
    }

    if (validationErrors.isEmpty()) {
        return
    }

    validationErrors.forEach { message ->
        logger.error(message)
    }
    throw IllegalStateException(validationErrors.joinToString(separator = "\n"))
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
        legacyValue,
        title,
        routePath,
        icon,
        order.toString(),
        qualifiedName,
        simpleName,
        sceneId,
        sceneName,
        sceneIcon,
        sceneOrder.toString(),
        menuPath.joinToString("\u001F"),
        defaultInScene.toString(),
    ).joinToString("|") { encodeSnapshotField(it) }
}

private fun decodeSnapshotLine(line: String): RouteRecord? {
    val parts = line.split('|')
    return when (parts.size) {
        7 -> decodeV1Snapshot(parts)
        13 -> decodeV2Snapshot(parts)
        else -> null
    }
}

private fun decodeV1Snapshot(parts: List<String>): RouteRecord {
    return RouteRecord(
        legacyValue = decodeSnapshotField(parts[0]),
        title = decodeSnapshotField(parts[1]),
        routePath = decodeSnapshotField(parts[2]),
        icon = decodeSnapshotField(parts[3]),
        order = decodeSnapshotField(parts[4]).toDoubleOrNull() ?: 0.0,
        qualifiedName = decodeSnapshotField(parts[5]),
        simpleName = decodeSnapshotField(parts[6]),
        sceneId = "",
        sceneName = "",
        sceneIcon = "Apps",
        sceneOrder = Int.MAX_VALUE,
        menuPath = emptyList(),
        defaultInScene = false,
    )
}

private fun decodeV2Snapshot(parts: List<String>): RouteRecord {
    return RouteRecord(
        legacyValue = decodeSnapshotField(parts[0]),
        title = decodeSnapshotField(parts[1]),
        routePath = decodeSnapshotField(parts[2]),
        icon = decodeSnapshotField(parts[3]),
        order = decodeSnapshotField(parts[4]).toDoubleOrNull() ?: 0.0,
        qualifiedName = decodeSnapshotField(parts[5]),
        simpleName = decodeSnapshotField(parts[6]),
        sceneId = decodeSnapshotField(parts[7]),
        sceneName = decodeSnapshotField(parts[8]),
        sceneIcon = decodeSnapshotField(parts[9]).ifBlank { "Apps" },
        sceneOrder = decodeSnapshotField(parts[10]).toIntOrNull() ?: Int.MAX_VALUE,
        menuPath = decodeSnapshotField(parts[11])
            .split('\u001F')
            .filter { segment -> segment.isNotBlank() },
        defaultInScene = decodeSnapshotField(parts[12]).toBoolean(),
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
