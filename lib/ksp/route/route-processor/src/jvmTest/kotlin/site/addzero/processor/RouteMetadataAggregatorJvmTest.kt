package site.addzero.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import site.addzero.context.Settings

class RouteMetadataAggregatorJvmTest {
    @Test
    fun renderRouteKeysCodeIncludesPlacementMetadata() {
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
                    title = "Dashboard",
                    routePath = "system/dashboard",
                    qualifiedName = "sample.DashboardScreen",
                    simpleName = "DashboardScreen",
                    sceneId = "system",
                    sceneName = "系统",
                    sceneIcon = "AdminPanelSettings",
                    sceneOrder = 100,
                    menuPath = listOf("控制台"),
                    defaultInScene = true,
                ),
            ),
        )

        assertContains(code, """import site.addzero.annotation.RoutePlacement""")
        assertContains(code, """RoutePlacement(scene = RouteScene(id = "system", name = "系统", icon = "AdminPanelSettings", order = 100), menuPath = arrayOf("控制台"), defaultInScene = true)""")
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
        snapshotDir.resolve("legacy.route-snapshot").writeText(
            buildString {
                appendLine("# addzero-route-snapshot:v1")
                appendLine(
                    encodeSnapshotLineV1(
                        legacyValue = "旧菜单",
                        title = "Legacy Screen",
                        routePath = "legacy/screen",
                        icon = "Apps",
                        order = 1.0,
                        qualifiedName = "legacy.LegacyScreen",
                        simpleName = "LegacyScreen",
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
                    title = "Fresh Screen",
                    routePath = "fresh/screen",
                    qualifiedName = "fresh.FreshScreen",
                    simpleName = "FreshScreen",
                    sceneId = "fresh-scene",
                    sceneName = "Fresh",
                    sceneIcon = "Apps",
                    sceneOrder = 10,
                    menuPath = listOf("Fresh Menu"),
                    defaultInScene = true,
                ),
            ),
            logger = TestKspLogger(),
        )

        val routeKeysCode = sharedSourceDir
            .resolve("site/addzero/generated/RouteKeys.kt")
            .readText()

        assertContains(routeKeysCode, "\"legacy/screen\"")
        assertContains(routeKeysCode, "\"fresh/screen\"")
        assertContains(routeKeysCode, """RoutePlacement(scene = RouteScene(id = "", name = "", icon = "Apps", order = 2147483647), menuPath = emptyArray(), defaultInScene = false)""")
        assertContains(routeKeysCode, """RoutePlacement(scene = RouteScene(id = "fresh-scene", name = "Fresh", icon = "Apps", order = 10), menuPath = arrayOf("Fresh Menu"), defaultInScene = true)""")
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
                appendLine("# addzero-route-snapshot:v2")
                appendLine(
                    routeRecord(
                        title = "Stale Screen",
                        routePath = "stale/screen",
                        qualifiedName = "stale.StaleScreen",
                        simpleName = "StaleScreen",
                    ).encodeSnapshotLine(),
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

        kotlin.test.assertFalse(staleSnapshot.exists())
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
                        sceneId = "system",
                        sceneName = "系统",
                        sceneIcon = "AdminPanelSettings",
                        sceneOrder = 100,
                    ),
                    routeRecord(
                        title = "Two",
                        routePath = "system/two",
                        qualifiedName = "sample.TwoScreen",
                        simpleName = "TwoScreen",
                        sceneId = "system",
                        sceneName = "System",
                        sceneIcon = "Apps",
                        sceneOrder = 200,
                    ),
                ),
                logger = logger,
            )
        }

        assertContains(error.message.orEmpty(), "scene.id=system metadata mismatch")
        assertContains(logger.errors.single(), "scene.id=system metadata mismatch")
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
                        sceneId = "system",
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
                        sceneId = "system",
                        sceneName = "系统",
                        sceneIcon = "AdminPanelSettings",
                        sceneOrder = 100,
                        defaultInScene = true,
                    ),
                ),
                logger = logger,
            )
        }

        assertContains(error.message.orEmpty(), "scene.id=system has multiple default routes")
        assertContains(logger.errors.single(), "scene.id=system has multiple default routes")
    }
}

private fun routeRecord(
    title: String,
    routePath: String,
    qualifiedName: String,
    simpleName: String,
    sceneId: String = "",
    sceneName: String = "",
    sceneIcon: String = "Apps",
    sceneOrder: Int = Int.MAX_VALUE,
    menuPath: List<String> = emptyList(),
    defaultInScene: Boolean = false,
): RouteRecord {
    return RouteRecord(
        legacyValue = "",
        title = title,
        routePath = routePath,
        icon = "Apps",
        order = 0.0,
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        sceneId = sceneId,
        sceneName = sceneName,
        sceneIcon = sceneIcon,
        sceneOrder = sceneOrder,
        menuPath = menuPath,
        defaultInScene = defaultInScene,
    )
}

private fun encodeSnapshotLineV1(
    legacyValue: String,
    title: String,
    routePath: String,
    icon: String,
    order: Double,
    qualifiedName: String,
    simpleName: String,
): String {
    return listOf(
        legacyValue,
        title,
        routePath,
        icon,
        order.toString(),
        qualifiedName,
        simpleName,
    ).joinToString("|") { value ->
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))
    }
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
