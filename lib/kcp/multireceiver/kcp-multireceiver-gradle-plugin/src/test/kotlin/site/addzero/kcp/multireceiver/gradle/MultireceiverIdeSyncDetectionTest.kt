package site.addzero.kcp.multireceiver.gradle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MultireceiverIdeSyncDetectionTest {

    @Test
    fun disables_compiler_plugin_in_idea_process() {
        assertTrue(
            shouldDisableCompilerPluginForIde(
                systemProperties = mapOf("idea.active" to "true"),
                taskNames = listOf("compileKotlin"),
            ),
        )
        assertTrue(
            shouldDisableCompilerPluginForIde(
                systemProperties = mapOf("android.injected.invoked.from.ide" to "true"),
                taskNames = listOf("compileKotlin"),
            ),
        )
    }

    @Test
    fun disables_compiler_plugin_for_sync_signals() {
        assertTrue(
            shouldDisableCompilerPluginForIde(
                systemProperties = mapOf("idea.sync.active" to "true"),
                taskNames = emptyList(),
            ),
        )
        assertTrue(
            shouldDisableCompilerPluginForIde(
                systemProperties = emptyMap(),
                taskNames = listOf("prepareKotlinIdeaImport"),
            ),
        )
    }

    @Test
    fun keeps_compiler_plugin_enabled_for_regular_builds() {
        assertFalse(
            shouldDisableCompilerPluginForIde(
                systemProperties = mapOf(
                    "idea.active" to "false",
                    "idea.sync.active" to "false",
                ),
                taskNames = listOf("compileKotlin", "test"),
            ),
        )
    }
}
