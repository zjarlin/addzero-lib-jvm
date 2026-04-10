package sample

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Controller2ApiGeneratedBridgeSmokeTest {
    @Test
    fun generatedAggregatorSourceUsesDefaultApisObjectInSamePackage() {
        val projectDir = System.getProperty("controller2api.smoke.projectDir")
            ?: error("Missing controller2api.smoke.projectDir")
        val apiFile = File(
            projectDir,
            "build/generated/source/controller2api/commonMain/kotlin/" +
                "sample/api/external/generated/SmokeApi.kt",
        )
        val aggregatorFile = File(
            projectDir,
            "build/generated/source/controller2api/commonMain/kotlin/" +
                "sample/api/external/generated/Apis.kt",
        )
        val moduleFile = File(
            projectDir,
            "build/generated/source/controller2api/commonMain/kotlin/" +
                "sample/api/external/generated/ApisModule.kt",
        )
        val bridgeFile = File(
            projectDir,
            "build/generated/source/controller2api/commonMain/kotlin/" +
                "sample/api/bridge/generated/Controller2ApiGeneratedClients.kt",
        )

        assertTrue(apiFile.exists(), "SmokeApi.kt 应当被生成到 apiClientOutputDir")
        assertTrue(aggregatorFile.exists(), "Apis.kt 应当被生成到 apiClientAggregatorOutputDir")
        assertTrue(moduleFile.exists(), "ApisModule.kt 应当被生成到 apiClientAggregatorOutputDir")
        assertFalse(bridgeFile.exists(), "未配置 bridge 参数时不应残留 Controller2ApiGeneratedClients.kt")

        val apiSource = apiFile.readText()
        val aggregatorSource = aggregatorFile.readText()
        val moduleSource = moduleFile.readText()
        assertContains(apiSource, "package sample.api.external.generated")
        assertContains(apiSource, "interface SmokeApi")
        assertContains(aggregatorSource, "package sample.api.external.generated")
        assertContains(aggregatorSource, "object Apis")
        assertContains(aggregatorSource, "import sample.api.external.generated.createSmokeApi")
        assertContains(aggregatorSource, "fun smokeApi(ktorfit: Ktorfit): SmokeApi")
        assertContains(aggregatorSource, "return ktorfit.createSmokeApi()")
        assertContains(moduleSource, "package sample.api.external.generated")
        assertContains(moduleSource, "@Configuration")
        assertContains(moduleSource, "@Module")
        assertContains(moduleSource, "class ApisModule")
        assertContains(moduleSource, "fun smokeApi(ktorfit: Ktorfit): SmokeApi")
        assertContains(moduleSource, "return Apis.smokeApi(ktorfit)")
    }
}
