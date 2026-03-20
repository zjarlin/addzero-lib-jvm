package site.addzero.kcp.allobjectjvmstatic.gradle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AllObjectJvmStaticGradleSubpluginTest {

    @Test
    fun disables_plugin_for_ide_sync_tasks() {
        assertTrue(shouldDisableCompilerPluginForIdeSync(emptyMap(), listOf("ideaSyncTask")))
        assertTrue(shouldDisableCompilerPluginForIdeSync(emptyMap(), listOf("prepareKotlinIdeaImport")))
        assertTrue(shouldDisableCompilerPluginForIdeSync(emptyMap(), listOf("jvmSyncTask")))
    }

    @Test
    fun keeps_plugin_enabled_for_normal_tasks() {
        assertFalse(shouldDisableCompilerPluginForIdeSync(emptyMap(), listOf("build", "test")))
    }
}
