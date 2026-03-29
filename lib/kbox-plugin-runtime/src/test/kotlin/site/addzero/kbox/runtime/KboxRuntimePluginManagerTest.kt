package site.addzero.kbox.runtime

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.plugin.api.KboxInstalledPluginState
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.File

class KboxRuntimePluginManagerTest {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `install disable enable uninstall should update routes and snapshots`() = runBlocking {
        val tempAppDataDir = createTempDirectory("kbox-runtime-manager").toFile()
        val pathService = testPathService(tempAppDataDir)
        startTestKoin(pathService)
        val manager = KboxRuntimePluginManager(pathService = pathService, json = json)

        val installResult = manager.installFromDirectory(runtimeFixtureDir().absolutePath)

        assertTrue(installResult.success, installResult.message)
        assertEquals(listOf("runtime/hello-runtime"), manager.dynamicRoutes.value.map { route -> route.routePath })
        assertEquals(
            KboxInstalledPluginState.LOADED,
            manager.installedPlugins.value.single { snapshot -> snapshot.pluginId == "hello-runtime" }.state,
        )

        val disableResult = manager.disable("hello-runtime")

        assertTrue(disableResult.success, disableResult.message)
        assertTrue(manager.dynamicRoutes.value.isEmpty())
        assertEquals(
            KboxInstalledPluginState.DISABLED,
            manager.installedPlugins.value.single { snapshot -> snapshot.pluginId == "hello-runtime" }.state,
        )

        val enableResult = manager.enable("hello-runtime")

        assertTrue(enableResult.success, enableResult.message)
        assertEquals(listOf("runtime/hello-runtime"), manager.dynamicRoutes.value.map { route -> route.routePath })
        assertEquals(
            KboxInstalledPluginState.LOADED,
            manager.installedPlugins.value.single { snapshot -> snapshot.pluginId == "hello-runtime" }.state,
        )

        val pluginDir = File(pathService.appDataDir(), "plugins/hello-runtime")
        val uninstallResult = manager.uninstall("hello-runtime")

        assertTrue(uninstallResult.success, uninstallResult.message)
        assertFalse(pluginDir.exists())
        assertTrue(manager.dynamicRoutes.value.isEmpty())
        assertTrue(manager.installedPlugins.value.none { snapshot -> snapshot.pluginId == "hello-runtime" })
    }

    @Test
    fun `install should reject incompatible host api version`() = runBlocking {
        val tempAppDataDir = createTempDirectory("kbox-runtime-manager-version").toFile()
        val pathService = testPathService(tempAppDataDir)
        startTestKoin(pathService)
        val manager = KboxRuntimePluginManager(pathService = pathService, json = json)
        val incompatibleDir = createTempDirectory("kbox-runtime-incompatible").toFile()

        runtimeFixtureDir().copyRecursively(incompatibleDir, overwrite = true)
        val manifestFile = File(incompatibleDir, "plugin.json")
        manifestFile.writeText(
            manifestFile.readText().replace(
                "\"hostApiVersion\": \"1.0\"",
                "\"hostApiVersion\": \"9.9\"",
            ),
        )

        val error = assertFailsWith<IllegalArgumentException> {
            manager.installFromDirectory(incompatibleDir.absolutePath)
        }

        assertTrue(error.message.orEmpty().contains("插件 API 版本不兼容"))
        assertTrue(manager.installedPlugins.value.isEmpty())
        assertTrue(manager.dynamicRoutes.value.isEmpty())
    }

    @Test
    fun `refresh should expose incompatible installed plugin as failed snapshot`() = runBlocking {
        val tempAppDataDir = createTempDirectory("kbox-runtime-manager-refresh").toFile()
        val pathService = testPathService(tempAppDataDir)
        startTestKoin(pathService)
        val manager = KboxRuntimePluginManager(pathService = pathService, json = json)
        val installedDir = File(pathService.appDataDir(), "plugins/incompatible-runtime")

        runtimeFixtureDir().copyRecursively(installedDir, overwrite = true)
        File(installedDir, "plugin.json").writeText(
            File(installedDir, "plugin.json").readText()
                .replace("\"pluginId\": \"hello-runtime\"", "\"pluginId\": \"incompatible-runtime\"")
                .replace("\"hostApiVersion\": \"1.0\"", "\"hostApiVersion\": \"9.9\""),
        )

        manager.refresh()

        val snapshot = manager.installedPlugins.value.single { it.pluginId == "incompatible-runtime" }
        assertEquals(KboxInstalledPluginState.FAILED, snapshot.state)
        assertTrue(snapshot.lastError.contains("插件 API 版本不兼容"))
        assertTrue(manager.dynamicRoutes.value.isEmpty())
    }

    private fun startTestKoin(
        pathService: KboxPathService,
    ) {
        startKoin {
            modules(
                module {
                    single<Json> { json }
                    single<KboxPathService> { pathService }
                },
            )
        }
    }

    private fun runtimeFixtureDir(): File {
        val path = System.getProperty("kbox.runtimeFixtureDir").orEmpty()
        assertTrue(path.isNotBlank(), "缺少运行时插件夹具目录")
        val fixtureDir = File(path)
        assertTrue(fixtureDir.isDirectory, "运行时插件夹具目录不存在：$path")
        assertNotNull(File(fixtureDir, "plugin.json").takeIf(File::isFile))
        return fixtureDir
    }

    private fun testPathService(
        appDataDir: File,
    ): KboxPathService {
        return object : KboxPathService() {
            override fun appDataDir(): File {
                return appDataDir.apply { mkdirs() }
            }
        }
    }
}
