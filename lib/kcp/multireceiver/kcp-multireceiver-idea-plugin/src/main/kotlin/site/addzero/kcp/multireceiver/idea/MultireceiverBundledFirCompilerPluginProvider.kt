package site.addzero.kcp.multireceiver.idea

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.fir.extensions.KotlinBundledFirCompilerPluginProvider
import java.nio.file.Path

class MultireceiverBundledFirCompilerPluginProvider : KotlinBundledFirCompilerPluginProvider {

    private val logger = Logger.getInstance(MultireceiverBundledFirCompilerPluginProvider::class.java)

    override fun provideBundledPluginJar(project: Project, userSuppliedPluginJar: Path): Path? {
        if (!MultireceiverBundledCompilerPluginLocator.isMultireceiverCompilerPluginJar(userSuppliedPluginJar)) {
            return null
        }

        val pluginDescriptor = PluginManagerCore.getPlugin(
            PluginId.getId(MultireceiverIdeaConstants.ideaPluginId),
        )
        if (pluginDescriptor == null) {
            logger.warn("Unable to resolve IDEA plugin descriptor for ${MultireceiverIdeaConstants.ideaPluginId}")
            return null
        }

        val bundledJar = MultireceiverBundledCompilerPluginLocator.findBundledCompilerPluginJar(
            pluginDescriptor.pluginPath,
        )
        if (bundledJar == null) {
            logger.warn(
                "Unable to find bundled multireceiver compiler plugin jar under ${pluginDescriptor.pluginPath}",
            )
            return null
        }

        logger.info("Substituting multireceiver compiler plugin jar: $userSuppliedPluginJar -> $bundledJar")
        return bundledJar
    }
}
