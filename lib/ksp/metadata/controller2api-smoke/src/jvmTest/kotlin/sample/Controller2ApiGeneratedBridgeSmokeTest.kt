package sample

import de.jensklingenberg.ktorfit.Ktorfit
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import sample.api.external.generated.SmokeApi
import site.addzero.core.network.spi.HttpClientProfileSpi

@Module
class Controller2ApiSmokeTestModule {
    @Single
    fun httpClientProfileSpi(): HttpClientProfileSpi {
        return object : HttpClientProfileSpi {
            override val baseUrl: String = "https://example.com"
        }
    }
}

class Controller2ApiGeneratedBridgeSmokeTest {
    @Test
    fun generatedBridgeRegistersApiIntoKoinRoot() {
        val app = koinApplication {
            withConfiguration<Controller2ApiSmokeKoinApplication>()
            modules(Controller2ApiSmokeTestModule().module())
        }

        try {
            val ktorfit = app.koin.get<Ktorfit>()
            val smokeApi = app.koin.get<SmokeApi>()

            assertNotNull(ktorfit)
            assertNotNull(smokeApi)
        } finally {
            app.close()
        }
    }

    @Test
    fun generatedAggregatorSourceUsesDefaultApisObjectInSamePackage() {
        val projectDir = System.getProperty("controller2api.smoke.projectDir")
            ?: error("Missing controller2api.smoke.projectDir")
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

        val aggregatorSource = aggregatorFile.readText()
        val moduleSource = moduleFile.readText()
        assertContains(aggregatorSource, "package sample.api.external.generated")
        assertContains(aggregatorSource, "object Apis")
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
