package site.addzero.ksp.metadata.jimmer.entity.external

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorOptions
import site.addzero.lsi.processor.ProcessorSpi

class JimmerEntityExternalProcessorSupportTest {
    @Test
    fun `mergeCollectedEntities deduplicates by qualified name across rounds`() {
        val target = linkedMapOf(
            "demo.Device" to entity("demo.Device", "Device", "first")
        )

        JimmerEntityExternalProcessorSupport.mergeCollectedEntities(
            target = target,
            roundEntities = mapOf(
                "demo.Device" to entity("demo.Device", "Device", "second"),
                "demo.Profile" to entity("demo.Profile", "Profile")
            )
        )

        assertEquals(2, target.size)
        assertEquals("second", target.getValue("demo.Device").docComment)
    }

    @Test
    fun `filterEnabledProcessors and executeFinish only run enabled processors`() {
        val executed = mutableListOf<String>()
        val logger = TestKspLogger()
        val processors = listOf(
            TestProcessor(JimmerEntityProcessorIds.ENTITY2_ISO, onFinish = { executed += id }),
            TestProcessor(
                JimmerEntityProcessorIds.ENTITY2_FORM,
                dependsOn = setOf(JimmerEntityProcessorIds.ENTITY2_ISO),
                onFinish = { executed += id }
            ),
            TestProcessor(
                JimmerEntityProcessorIds.ENTITY2_MCP,
                dependsOn = setOf(JimmerEntityProcessorIds.ENTITY2_ISO),
                onFinish = { executed += id }
            )
        )
        val enabled = JimmerEntityExternalProcessorSupport.filterEnabledProcessors(
            processors = processors,
            options = mapOf(
                JimmerEntityProcessorOptions.ENTITY2_ISO_ENABLED to "true",
                JimmerEntityProcessorOptions.ENTITY2_FORM_ENABLED to "false",
                JimmerEntityProcessorOptions.ENTITY2_MCP_ENABLED to "false"
            )
        )
        val context = JimmerEntityProcessContext(
            logger = logger,
            options = emptyMap(),
            entitiesByQualifiedName = mapOf("demo.Device" to entity("demo.Device", "Device"))
        )

        JimmerEntityExternalProcessorSupport.executeFinish(
            sortedLayers = JimmerEntityExternalProcessorSupport.sortLayers(enabled),
            context = context,
            logger = logger
        )

        assertEquals(listOf(JimmerEntityProcessorIds.ENTITY2_ISO), executed)
    }

    @Test
    fun `sortLayers honors dependsOn and rejects missing dependency after filtering`() {
        val sortedLayers = JimmerEntityExternalProcessorSupport.sortLayers(
            linkedMapOf(
                JimmerEntityProcessorIds.ENTITY2_ISO to TestProcessor(JimmerEntityProcessorIds.ENTITY2_ISO),
                JimmerEntityProcessorIds.ENTITY2_FORM to TestProcessor(
                    id = JimmerEntityProcessorIds.ENTITY2_FORM,
                    dependsOn = setOf(JimmerEntityProcessorIds.ENTITY2_ISO)
                ),
                JimmerEntityProcessorIds.ENTITY2_MCP to TestProcessor(
                    id = JimmerEntityProcessorIds.ENTITY2_MCP,
                    dependsOn = setOf(JimmerEntityProcessorIds.ENTITY2_ISO)
                )
            )
        )
        assertContentEquals(
            expected = listOf(
                listOf(JimmerEntityProcessorIds.ENTITY2_ISO),
                listOf(JimmerEntityProcessorIds.ENTITY2_FORM, JimmerEntityProcessorIds.ENTITY2_MCP)
            ),
            actual = sortedLayers.map { layer -> layer.map { it.id } }
        )

        val enabled = JimmerEntityExternalProcessorSupport.filterEnabledProcessors(
            processors = listOf(
                TestProcessor(
                    id = JimmerEntityProcessorIds.ENTITY2_FORM,
                    dependsOn = setOf(JimmerEntityProcessorIds.ENTITY2_ISO)
                )
            ),
            options = mapOf(
                JimmerEntityProcessorOptions.ENTITY2_FORM_ENABLED to "true",
                JimmerEntityProcessorOptions.ENTITY2_ISO_ENABLED to "false"
            )
        )

        assertFailsWith<IllegalStateException> {
            JimmerEntityExternalProcessorSupport.sortLayers(enabled)
        }
    }
}

private fun entity(
    qualifiedName: String,
    simpleName: String,
    docComment: String = ""
): JimmerEntityMeta {
    return JimmerEntityMeta(
        qualifiedName = qualifiedName,
        packageName = qualifiedName.substringBeforeLast('.'),
        simpleName = simpleName,
        docComment = docComment
    )
}

private class TestProcessor(
    override val id: String,
    override val dependsOn: Set<String> = emptySet(),
    private val onFinish: TestProcessor.() -> Unit = {}
) : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override lateinit var ctx: JimmerEntityProcessContext

    override fun onFinish() {
        onFinish(this)
    }
}

private class TestKspLogger : KSPLogger {
    override fun logging(message: String, symbol: KSNode?) = Unit

    override fun info(message: String, symbol: KSNode?) = Unit

    override fun warn(message: String, symbol: KSNode?) = Unit

    override fun error(message: String, symbol: KSNode?) = Unit

    override fun exception(e: Throwable) {
        throw e
    }
}
