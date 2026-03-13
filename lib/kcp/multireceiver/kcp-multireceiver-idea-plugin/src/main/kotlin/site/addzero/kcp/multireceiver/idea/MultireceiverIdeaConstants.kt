package site.addzero.kcp.multireceiver.idea

internal object MultireceiverIdeaConstants {
    const val ideaPluginId = "site.addzero.kcp-multireceiver-idea-plugin"
    const val compilerPluginRegistrarClassName =
        "site.addzero.kcp.multireceiver.plugin.MultireceiverCompilerPluginRegistrar"
    const val compilerPluginRegistrarServiceFile =
        "META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar"
    const val bundledCompilerPluginJarPrefix = "kcp-multireceiver-plugin"
}
