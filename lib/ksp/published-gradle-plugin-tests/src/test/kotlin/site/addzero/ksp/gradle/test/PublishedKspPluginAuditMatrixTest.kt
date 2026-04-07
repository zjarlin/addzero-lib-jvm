package site.addzero.ksp.gradle.test

import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class PublishedKspPluginAuditMatrixTest {

    private val repoRoot = File(System.getProperty("publishedKsp.repoRoot"))

    @Test
    fun `retained removed demo and spi matrices stay distinct`() {
        val retained = PublishedKspPluginSpecs.retained.map { it.pluginId }.toSet()
        assertEquals(
            setOf(
                "site.addzero.ksp.compose-props",
                "site.addzero.ksp.gen-reified",
                "site.addzero.ksp.ioc",
                "site.addzero.ksp.jimmer-entity-external",
                "site.addzero.ksp.ksp-dsl-builder",
                "site.addzero.ksp.method-semanticizer",
                "site.addzero.ksp.modbus-rtu",
                "site.addzero.ksp.modbus-tcp",
                "site.addzero.ksp.multireceiver",
                "site.addzero.ksp.singleton-adapter",
                "site.addzero.ksp.spring2ktor-server",
                "site.addzero.ksp.route",
            ),
            retained,
        )
        assertTrue(retained.intersect(PublishedKspPluginSpecs.removed).isEmpty())
        assertTrue(retained.intersect(PublishedKspPluginSpecs.loggerDemo).isEmpty())
    }

    @Test
    fun `removed sibling plugin modules stay deleted while logger remains demo only`() {
        val removedBuildFiles = listOf(
            "lib/ksp/jdbc2metadata/jdbc2controller-gradle-plugin/build.gradle.kts",
            "lib/ksp/jdbc2metadata/jdbc2entity-gradle-plugin/build.gradle.kts",
            "lib/ksp/jdbc2metadata/jdbc2enum-gradle-plugin/build.gradle.kts",
            "lib/ksp/metadata/controller2api-gradle-plugin/build.gradle.kts",
            "lib/ksp/metadata/controller2feign-gradle-plugin/build.gradle.kts",
            "lib/ksp/metadata/controller2iso2dataprovider-gradle-plugin/build.gradle.kts",
            "lib/ksp/metadata/enum-gradle-plugin/build.gradle.kts",
        )
        removedBuildFiles.forEach { relativePath ->
            assertTrue(!repoRoot.resolve(relativePath).exists(), "$relativePath should be deleted")
        }
        assertTrue(repoRoot.resolve("lib/ksp/logger-gradle-plugin/build.gradle.kts").exists())
    }

    @Test
    fun `repo scan keeps openapi codegen as outlier outside lib ksp migration scope`() {
        assertTrue(
            repoRoot.resolve(
                "lib/openapi-codegen/src/commonMain/kotlin/site/addzero/kcloud/codegen/OpenApiCodegenProcessorProvider.kt",
            ).exists(),
        )
    }
}
