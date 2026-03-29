package site.addzero.configcenter.runtime

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigTargetKind
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueType

class ConfigCenterGatewayTest {
    @Test
    fun `local override should win over repo value`() {
        val gateway = newGateway()

        kotlinx.coroutines.runBlocking {
            gateway.addEnv(
                ConfigMutationRequest(
                    key = "app.name",
                    namespace = "demo",
                    domain = ConfigDomain.SYSTEM,
                    profile = "default",
                    valueType = ConfigValueType.STRING,
                    storageMode = ConfigStorageMode.REPO_PLAIN,
                    value = "repo-value",
                ),
            )
            gateway.addEnv(
                ConfigMutationRequest(
                    key = "app.name",
                    namespace = "demo",
                    domain = ConfigDomain.SYSTEM,
                    profile = "default",
                    valueType = ConfigValueType.STRING,
                    storageMode = ConfigStorageMode.LOCAL_OVERRIDE,
                    value = "local-value",
                ),
            )

            val resolved = gateway.getEnv(
                key = "app.name",
                query = ConfigQuery(namespace = "demo"),
            )

            assertEquals("local-value", resolved)
        }
    }

    @Test
    fun `encrypted entry should roundtrip with master key`() {
        val gateway = newGateway()

        kotlinx.coroutines.runBlocking {
            gateway.addEnv(
                ConfigMutationRequest(
                    key = "app.token",
                    namespace = "demo",
                    domain = ConfigDomain.SYSTEM,
                    profile = "default",
                    valueType = ConfigValueType.STRING,
                    storageMode = ConfigStorageMode.REPO_ENCRYPTED,
                    value = "secret-token",
                ),
            )

            val resolved = gateway.getEnv(
                key = "app.token",
                query = ConfigQuery(namespace = "demo"),
            )

            assertEquals("secret-token", resolved)
        }
    }

    @Test
    fun `json target should render nested structure`() {
        val gateway = newGateway()

        kotlinx.coroutines.runBlocking {
            gateway.addEnv(
                ConfigMutationRequest(
                    key = "ktor.deployment.port",
                    namespace = "demo",
                    domain = ConfigDomain.SYSTEM,
                    profile = "default",
                    valueType = ConfigValueType.INTEGER,
                    storageMode = ConfigStorageMode.REPO_PLAIN,
                    value = "8088",
                ),
            )
            gateway.addEnv(
                ConfigMutationRequest(
                    key = "ktor.deployment.host",
                    namespace = "demo",
                    domain = ConfigDomain.SYSTEM,
                    profile = "default",
                    valueType = ConfigValueType.STRING,
                    storageMode = ConfigStorageMode.REPO_PLAIN,
                    value = "127.0.0.1",
                ),
            )

            val savedTarget = gateway.saveTarget(
                ConfigTargetMutationRequest(
                    name = "demo-json",
                    targetKind = ConfigTargetKind.JSON_FILE,
                    outputPath = "",
                    namespaceFilter = "demo",
                    profile = "default",
                ),
            )

            val rendered = gateway.renderTarget(savedTarget.id)
            assertTrue(rendered.content.contains("\"ktor\""))
            assertTrue(rendered.content.contains("\"port\": 8088"))
        }
    }

    @Test
    fun `bootstrap should use explicit db path`() {
        val tempDb = Files.createTempFile("config-center-bootstrap", ".sqlite").toFile()
        val bootstrap = ConfigCenterBootstrap(
            ConfigCenterBootstrapOptions(
                dbPath = tempDb.absolutePath,
                masterKey = "test-master-key",
                appId = "demo",
            ),
        )

        assertEquals(tempDb.absolutePath, bootstrap.dbFile.absolutePath)
        assertEquals("demo", bootstrap.appId)
        assertEquals("test-master-key", bootstrap.masterKey)
    }

    private fun newGateway(): JvmConfigCenterGateway {
        val tempDb = Files.createTempFile("config-center-test", ".sqlite").toFile()
        tempDb.delete()
        val gateway = JvmConfigCenterGateway.createDefault(
            ConfigCenterBootstrapOptions(
                dbPath = tempDb.absolutePath,
                masterKey = "unit-test-master-key",
                appId = "demo",
            ),
        )
        assertNotNull(gateway)
        return gateway
    }
}
