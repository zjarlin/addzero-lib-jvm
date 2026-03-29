package site.addzero.configcenter.runtime

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigTargetKind
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueType

class ConfigCenterRuntimeJvmTest {
    @Test
    fun `encrypted entry round trips and does not persist plain text`() {
        withRuntimeFixture { fixture ->
            runBlocking {
                fixture.gateway.addEnv(
                    ConfigMutationRequest(
                        key = "secret.token",
                        namespace = "demo",
                        domain = ConfigDomain.SYSTEM,
                        valueType = ConfigValueType.STRING,
                        storageMode = ConfigStorageMode.REPO_ENCRYPTED,
                        value = "token-123",
                    ),
                )
            }

            val stored = fixture.database.withConnection { connection ->
                connection.prepareStatement(
                    "SELECT cipher_text, plain_text FROM config_entry WHERE namespace = ? AND key = ?",
                ).use { statement ->
                    statement.setString(1, "demo")
                    statement.setString(2, "secret.token")
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            resultSet.getString("cipher_text") to resultSet.getString("plain_text")
                        } else {
                            null
                        }
                    }
                }
            }

            val resolved = runBlocking {
                fixture.gateway.getEnv(
                    key = "secret.token",
                    query = ConfigQuery(namespace = "demo"),
                )
            }

            assertNotNull(stored)
            assertNotNull(stored.first)
            assertNull(stored.second)
            assertEquals("token-123", resolved)
        }
    }

    @Test
    fun `local override wins over repo value`() {
        withRuntimeFixture { fixture ->
            runBlocking {
                fixture.gateway.addEnv(
                    ConfigMutationRequest(
                        key = "app.name",
                        namespace = "demo",
                        domain = ConfigDomain.BUSINESS,
                        valueType = ConfigValueType.STRING,
                        storageMode = ConfigStorageMode.REPO_PLAIN,
                        value = "repo-name",
                    ),
                )
                fixture.gateway.addEnv(
                    ConfigMutationRequest(
                        key = "app.name",
                        namespace = "demo",
                        domain = ConfigDomain.BUSINESS,
                        valueType = ConfigValueType.STRING,
                        storageMode = ConfigStorageMode.LOCAL_OVERRIDE,
                        value = "local-name",
                    ),
                )
            }

            val envValue = runBlocking {
                fixture.gateway.getEnv(
                    key = "app.name",
                    query = ConfigQuery(namespace = "demo"),
                )
            }
            val snapshotValue = runBlocking {
                fixture.gateway.getSnapshot(namespace = "demo")["app.name"]
            }

            assertEquals("local-name", envValue)
            assertEquals("local-name", snapshotValue)
        }
    }

    @Test
    fun `template target can preview and export`() {
        withRuntimeFixture { fixture ->
            runBlocking {
                fixture.gateway.addEnv(
                    ConfigMutationRequest(
                        key = "docker.compose.projectName",
                        namespace = "demo",
                        domain = ConfigDomain.SYSTEM,
                        valueType = ConfigValueType.STRING,
                        storageMode = ConfigStorageMode.REPO_PLAIN,
                        value = "config-center-demo",
                    ),
                )
            }

            val outputFile = File(fixture.tempDir, "docker-compose.generated.yml")
            val target = runBlocking {
                fixture.gateway.saveTarget(
                    ConfigTargetMutationRequest(
                        name = "Compose",
                        targetKind = ConfigTargetKind.DOCKER_COMPOSE_TEMPLATE,
                        outputPath = outputFile.absolutePath,
                        namespaceFilter = "demo",
                        templateText = """
services:
  demo:
    container_name: "{{docker.compose.projectName}}"
""".trimIndent(),
                    ),
                )
            }

            val preview = runBlocking { fixture.gateway.previewTarget(target.id) }
            val exported = runBlocking { fixture.gateway.exportTarget(target.id) }

            assertTrue(preview.contains("config-center-demo"))
            assertTrue(outputFile.isFile)
            assertEquals(exported.content, outputFile.readText())
            assertTrue(exported.exportedAtEpochMillis > 0L)
        }
    }
}

private data class RuntimeFixture(
    val tempDir: File,
    val database: ConfigCenterDatabase,
    val gateway: JvmConfigCenterGateway,
)

private fun withRuntimeFixture(
    block: (RuntimeFixture) -> Unit,
) {
    val tempDir = Files.createTempDirectory("config-center-runtime-test-").toFile()
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
    val database = ConfigCenterDatabase(bootstrap)
    val repository = JdbcConfigCenterRepository(
        database = database,
        encryption = EnvMasterKeyEncryptionSpi(bootstrap),
        json = json,
    )
    val gateway = JvmConfigCenterGateway(
        bootstrap = bootstrap,
        repository = repository,
        renderers = listOf(DefaultConfigRendererSpi(json)),
    )

    try {
        block(RuntimeFixture(tempDir, database, gateway))
    } finally {
        tempDir.deleteRecursively()
    }
}
