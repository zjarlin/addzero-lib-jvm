package site.addzero.kcp.transformoverload.gradle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TransformOverloadIdeSyncDetectionTest {

    @Test
    fun disables_compiler_plugin_when_idea_sync_property_is_true() {
        assertTrue(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = mapOf("idea.sync.active" to "true"),
                taskNames = emptyList(),
            ),
        )
    }

    @Test
    fun disables_compiler_plugin_when_running_under_idea() {
        assertTrue(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = mapOf("idea.active" to "true"),
                taskNames = listOf("compileKotlin"),
            ),
        )
        assertTrue(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = mapOf("android.injected.invoked.from.ide" to "true"),
                taskNames = listOf("compileKotlin"),
            ),
        )
    }

    @Test
    fun disables_compiler_plugin_for_known_sync_tasks() {
        assertTrue(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = emptyMap(),
                taskNames = listOf("prepareKotlinIdeaImport"),
            ),
        )
        assertTrue(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = emptyMap(),
                taskNames = listOf("jvmSyncTask"),
            ),
        )
    }

    @Test
    fun keeps_compiler_plugin_enabled_for_normal_build_tasks() {
        assertFalse(
            shouldDisableCompilerPluginForIdeSync(
                systemProperties = mapOf("idea.sync.active" to "false"),
                taskNames = listOf("compileKotlin", "test"),
            ),
        )
    }
}
