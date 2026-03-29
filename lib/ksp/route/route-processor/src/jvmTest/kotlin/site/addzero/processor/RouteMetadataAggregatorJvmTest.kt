package site.addzero.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import java.nio.file.Files
import java.util.Base64
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import site.addzero.route.processor.context.Settings

class RouteMetadataAggregatorJvmTest {
    @Test
    fun renderRouteKeysCodeIncludesSceneAndParentMetadata() {
        Settings.fromOptions(
            mapOf(
                "sharedSourceDir" to "/tmp/shared/src/commonMain/kotlin",
                "routeGenPkg" to "site.addzero.generated",
                "routeOwnerModule" to "/tmp/owner/src/commonMain/kotlin",
            ),
        )

        val code = renderRouteKeysCode(
            listOf(
                routeRecord(
                    parentName = "控制台",
                    title = "Dashboard",
                    routePath = "system/dashboard",
                    qualifiedName = "sample.DashboardScreen",
                    simpleName = "DashboardScreen",
                    sceneName = "系统",
                    sceneIcon = "AdminPanelSettings",
                    sceneOrder = 100,
                    defaultInScene = true,
                ),
            ),
        )

        assertContains(code, """import site.addzero.annotation.RoutePlacement""")
        assertContains(code, "Route(value = \"控制台\"")
        assertContains(
            code,
            """RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = true)""",
        )
    }

    @Test
    fun aggregateAndGenerateRoutesReadsV1AndV2SnapshotsTogether() {
        val tempRoot = Files.createTempDirectory("route-processor-test")
        val sharedSourceDir = tempRoot.resolve("shared/src/commonMain/kotlin").createDirectories()
        val ownerSourceDir = tempRoot.resolve("owner/src/commonMain/kotlin").createDirectories()
        val snapshotDir = tempRoot
            .resolve("shared/build/addzero/route-processor/site/addzero/generated/snapshots")
            .createDirectories()
        Settings.fromOptions(
            mapOf(
                "sharedSourceDir" to sharedSourceDir.toString(),
                "routeGenPkg" to "site.addzero.generated",
                "routeOwnerModule" to ownerSourceDir.toString(),
            ),
        )
        snapshotDir.resolve("legacy-v1.route-snapshot").writeText(
            buildString {
                appendLine("# addzero-route-snapshot:v1")
                appendLine(
                    encodeSnapshotLineV1(
                        parentName = "旧菜单",
                        title = "Legacy V1 Screen",
                        routePath = "legacy/v1-screen",
                        icon = "Apps",
                        order = 1.0,
                        qualifiedName = "legacy.LegacyV1Screen",
                        simpleName = "LegacyV1Screen",
                    ),
                )
            },
        )
        snapshotDir.resolve("legacy-v2.route-snapshot").writeText(
            buildString {
                appendLine("# addzero-route-snapshot:v2")
                appendLine(
                    encodeSnapshotLineV2(
                        legacyValue = "",
                        title = "Legacy V2 Screen",
                        routePath = "legacy/v2-screen",
                        icon = "Apps",
                        order = 2.0,
                        qualifiedName = "legacy.LegacyV2Screen",
                        simpleName = "LegacyV2Screen",
                        sceneId = "legacy-scene",
                        sceneName = "Legacy",
                        sceneIcon = "Apps",
                        sceneOrder = 20,
                        menuPath = listOf("Legacy Root", "Legacy Group"),
                        defaultInScene = true,
                    ),
                )
            },
        )

        aggregateAndGenerateRoutes(
            sharedSourceDir = sharedSourceDir.toString(),
            routeGenPkg = "site.addzero.generated",
            routeOwnerModuleDir = ownerSourceDir.toString(),
            moduleKeyHint = "feature-fresh",
            moduleSourceRoots = listOf(tempRoot.resolve("feature/src/commonMain/kotlin").toString()),
            routeItems = listOf(
                routeRecord(
                    parentName = "Fresh Menu",
                    title = "Fresh Screen",
                    routePath = "fresh/screen",
                    qualifiedName = "fresh.FreshScreen",
                    simpleName = "FreshScreen",
                    sceneName = "Fresh",
                    sceneIcon = "Apps",
                    sceneOrder = 10,
                    defaultInScene = true,
                ),
            ),
            logger = TestKspLogger(),
        )

        val routeKeysCode = sharedSourceDir
            .resolve("site/addzero/generated/RouteKeys.kt")
            .readText()

        assertContains(routeKeysCode, "\"legacy/v1-screen\"")
        assertContains(routeKeysCode, "\"legacy/v2-screen\"")
        assertContains(routeKeysCode, "\"fresh/screen\"")
        assertContains(routeKeysCode, "Route(value = \"旧菜单\"")
        assertContains(routeKeysCode, "Route(value = \"Legacy Root/Legacy Group\"")
        assertContains(routeKeysCode, """RoutePlacement(scene = RouteScene(name = "Legacy", icon = "Apps", order = 20), defaultInScene = true)""")
        assertContains(routeKeysCode, """RoutePlacement(scene = RouteScene(name = "Fresh", icon = "Apps", order = 10), defaultInScene = true)""")
    }

    @Test
    fun aggregateAndGenerateRoutesUsesModuleKeyHintForEmptyModuleCleanup() {
        val tempRoot = Files.createTempDirectory("route-processor-cleanup")
        val sharedSourceDir = tempRoot.resolve("shared/src/commonMain/kotlin").createDirectories()
        val ownerSourceDir = tempRoot.resolve("owner/src/commonMain/kotlin").createDirectories()
        val snapshotDir = tempRoot
            .resolve("shared/build/addzero/route-processor/site/addzero/generated/snapshots")
            .createDirectories()
        val staleSnapshot = snapshotDir.resolve("apps_kcloud_plugins_system_ai-chat.route-snapshot")
        Settings.fromOptions(
            mapOf(
                "sharedSourceDir" to sharedSourceDir.toString(),
                "routeGenPkg" to "site.addzero.generated",
                "routeOwnerModule" to ownerSourceDir.toString(),
            ),
        )
        staleSnapshot.writeText(
            buildString {
                appendLine("# addzero-route-snapshot:v3")
                appendLine(
                    encodeSnapshotLineV3(
                        parentName = "",
                        title = "Stale Screen",
                        routePath = "stale/screen",
                        icon = "Apps",
                        order = 0.0,
                        qualifiedName = "stale.StaleScreen",
                        simpleName = "StaleScreen",
                        sceneName = "",
                        sceneIcon = "Apps",
                        sceneOrder = Int.MAX_VALUE,
                        defaultInScene = false,
                    ),
                )
            },
        )

        aggregateAndGenerateRoutes(
            sharedSourceDir = sharedSourceDir.toString(),
            routeGenPkg = "site.addzero.generated",
            routeOwnerModuleDir = ownerSourceDir.toString(),
            moduleKeyHint = "apps:kcloud:plugins:system:ai-chat",
            moduleSourceRoots = emptyList(),
            routeItems = emptyList(),
            logger = TestKspLogger(),
        )

        kotlin.test.assertFalse(Files.exists(staleSnapshot))
    }

    @Test
    fun validateMergedRoutesRejectsSceneMetadataMismatch() {
        val logger = TestKspLogger()

        val error = assertFailsWith<IllegalStateException> {
            validateMergedRoutes(
                listOf(
                    routeRecord(
                        title = "One",
                        routePath = "system/one",
                        qualifiedName = "sample.OneScreen",
                        simpleName = "OneScreen",
                        sceneName = "系统",
                        sceneIcon = "AdminPanelSettings",
                        sceneOrder = 100,
                    ),
                    routeRecord(
                        title = "Two",
                        routePath = "system/two",
                        qualifiedName = "sample.TwoScreen",
                        simpleName = "TwoScreen",
                        sceneName = "系统",
                        sceneIcon = "Apps",
                        sceneOrder = 200,
                    ),
                ),
                logger = logger,
            )
        }

        assertContains(error.message.orEmpty(), "scene.name=系统 metadata mismatch")
        assertContains(logger.errors.single(), "scene.name=系统 metadata mismatch")
    }

    @Test
    fun validateMergedRoutesRejectsMultipleSceneDefaults() {
        val logger = TestKspLogger()

        val error = assertFailsWith<IllegalStateException> {
            validateMergedRoutes(
                listOf(
                    routeRecord(
                        title = "One",
                        routePath = "system/one",
                        qualifiedName = "sample.OneScreen",
                        simpleName = "OneScreen",
                        sceneName = "系统",
                        sceneIcon = "AdminPanelSettings",
                        sceneOrder = 100,
                        defaultInScene = true,
                    ),
                    routeRecord(
                        title = "Two",
                        routePath = "system/two",
                        qualifiedName = "sample.TwoScreen",
                        simpleName = "TwoScreen",
                        sceneName = "系统",
                        sceneIcon = "AdminPanelSettings",
                        sceneOrder = 100,
                        defaultInScene = true,
                    ),
                ),
                logger = logger,
            )
        }

        assertContains(error.message.orEmpty(), "scene.name=系统 has multiple default routes")
        assertContains(logger.errors.single(), "scene.name=系统 has multiple default routes")
    }
}

private fun routeRecord(
    parentName: String = "",
    title: String,
    routePath: String,
    qualifiedName: String,
    simpleName: String,
    sceneName: String = "",
    sceneIcon: String = "Apps",
    sceneOrder: Int = Int.MAX_VALUE,
    defaultInScene: Boolean = false,
): RouteRecord {
    return RouteRecord(
        parentName = parentName,
        title = title,
        routePath = routePath,
        icon = "Apps",
        order = 0.0,
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        sceneName = sceneName,
        sceneIcon = sceneIcon,
        sceneOrder = sceneOrder,
        defaultInScene = defaultInScene,
    )
}

private fun encodeSnapshotLineV1(
    parentName: String,
    title: String,
    routePath: String,
    icon: String,
    order: Double,
    qualifiedName: String,
    simpleName: String,
): String {
    return listOf(
        parentName,
        title,
        routePath,
        icon,
        order.toString(),
        qualifiedName,
        simpleName,
    ).joinToString("|", transform = ::encodeSnapshotField)
}

private fun encodeSnapshotLineV2(
    legacyValue: String,
    title: String,
    routePath: String,
    icon: String,
    order: Double,
    qualifiedName: String,
    simpleName: String,
    sceneId: String,
    sceneName: String,
    sceneIcon: String,
    sceneOrder: Int,
    menuPath: List<String>,
    defaultInScene: Boolean,
): String {
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
    ).joinToString("|", transform = ::encodeSnapshotField)
}

private fun encodeSnapshotLineV3(
    parentName: String,
    title: String,
    routePath: String,
    icon: String,
    order: Double,
    qualifiedName: String,
    simpleName: String,
    sceneName: String,
    sceneIcon: String,
    sceneOrder: Int,
    defaultInScene: Boolean,
): String {
    return listOf(
        parentName,
        title,
        routePath,
        icon,
        order.toString(),
        qualifiedName,
        simpleName,
        sceneName,
        sceneIcon,
        sceneOrder.toString(),
        defaultInScene.toString(),
    ).joinToString("|", transform = ::encodeSnapshotField)
}

private fun encodeSnapshotField(value: String): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(Charsets.UTF_8))
}

private class TestKspLogger : KSPLogger {
    val errors = mutableListOf<String>()

    override fun logging(message: String, symbol: KSNode?) = Unit

    override fun info(message: String, symbol: KSNode?) = Unit

    override fun warn(message: String, symbol: KSNode?) = Unit

    override fun error(message: String, symbol: KSNode?) {
        errors += message
    }

    override fun exception(e: Throwable) {
        throw e
    }
}
