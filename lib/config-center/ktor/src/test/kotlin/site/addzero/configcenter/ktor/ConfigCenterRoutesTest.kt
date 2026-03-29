package site.addzero.configcenter.ktor

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import site.addzero.configcenter.runtime.ConfigCenterBootstrap
import site.addzero.configcenter.runtime.ConfigCenterBootstrapOptions
import site.addzero.configcenter.runtime.ConfigCenterDatabase
import site.addzero.configcenter.runtime.DefaultConfigRendererSpi
import site.addzero.configcenter.runtime.EnvMasterKeyEncryptionSpi
import site.addzero.configcenter.runtime.JdbcConfigCenterRepository
import site.addzero.configcenter.runtime.JvmConfigCenterGateway
import site.addzero.configcenter.spec.ConfigCenterGateway

class ConfigCenterRoutesTest {
    @Test
    fun `routes support entry target preview and bootstrap lookup`() {
        withRouteFixture { fixture ->
            testApplication {
                application {
                    install(ContentNegotiation) {
                        json()
                    }
                    routing {
                        configCenterRoutes()
                    }
                }

                val createEntryResponse = client.post("/api/config-center/entries") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                          "key":"app.name",
                          "namespace":"demo",
                          "domain":"SYSTEM",
                          "profile":"default",
                          "valueType":"STRING",
                          "storageMode":"REPO_PLAIN",
                          "value":"config-center"
                        }
                        """.trimIndent(),
                    )
                }
                val createTargetResponse = client.post("/api/config-center/targets") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                          "name":"Demo Template",
                          "targetKind":"GENERIC_TEXT_TEMPLATE",
                          "outputPath":"",
                          "namespaceFilter":"demo",
                          "profile":"default",
                          "templateText":"name={{app.name}}",
                          "enabled":true,
                          "sortOrder":10
                        }
                        """.trimIndent(),
                    )
                }
                val targetId = "\"id\":\"([^\"]+)\"".toRegex()
                    .find(createTargetResponse.bodyAsText())
                    ?.groupValues
                    ?.getOrNull(1)
                    .orEmpty()
                val envResponse = client.get("/api/config-center/env?key=app.name&namespace=demo")
                val previewResponse = client.post("/api/config-center/render/$targetId/preview")
                val bootstrapResponse = client.get("/api/config-center/bootstrap/CONFIG_CENTER_APP_ID")

                assertEquals(HttpStatusCode.OK, createEntryResponse.status)
                assertEquals(HttpStatusCode.OK, createTargetResponse.status)
                assertEquals(HttpStatusCode.OK, envResponse.status)
                assertEquals(HttpStatusCode.OK, previewResponse.status)
                assertEquals(HttpStatusCode.OK, bootstrapResponse.status)
                assertTrue(targetId.isNotBlank())
                assertTrue(envResponse.bodyAsText().contains("config-center"))
                assertTrue(previewResponse.bodyAsText().contains("name=config-center"))
                assertTrue(bootstrapResponse.bodyAsText().contains("\"value\":\"demo\""))
            }
        }
    }
}

private data class RouteFixture(
    val tempDir: File,
)

private fun withRouteFixture(
    block: (RouteFixture) -> Unit,
) {
    val tempDir = Files.createTempDirectory("config-center-routes-test-").toFile()
    val dbFile = File(tempDir, "config-center.sqlite")
    val bootstrap = ConfigCenterBootstrap(
        ConfigCenterBootstrapOptions(
            dbPath = dbFile.absolutePath,
            masterKey = "test-master-key",
            appId = "demo",
            profile = "default",
        ),
    )
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    val gateway = JvmConfigCenterGateway(
        bootstrap = bootstrap,
        repository = JdbcConfigCenterRepository(
            database = ConfigCenterDatabase(bootstrap),
            encryption = EnvMasterKeyEncryptionSpi(bootstrap),
            json = json,
        ),
        renderers = listOf(DefaultConfigRendererSpi(json)),
    )

    startKoin {
        modules(
            module {
                single<ConfigCenterBootstrap> { bootstrap }
                single<ConfigCenterGateway> { gateway }
            },
        )
    }

    try {
        block(RouteFixture(tempDir))
    } finally {
        stopKoin()
        tempDir.deleteRecursively()
    }
}
