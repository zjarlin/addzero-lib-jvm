package site.addzero.buildlogic.intellij

import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask


plugins {
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
    id("site.addzero.buildlogic.intellij.intellij-base")
    id("site.addzero.buildlogic.intellij.intellij-info")
}

val libs = versionCatalogs.named("libs")
val sinceBuildVersion = libs.findVersion("intellij-since-build").get().requiredVersion
val untilBuildVersion = libs.findVersion("intellij-until-build").get().requiredVersion

tasks {
    patchPluginXml {
        sinceBuild.set(sinceBuildVersion)
        untilBuild.set(untilBuildVersion)
    }
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
tasks.named<RunIdeTask>("runIde") {
    jvmArgumentProviders += CommandLineArgumentProvider {
        listOf("-Didea.kotlin.plugin.use.k2=true")
    }
}
tasks.named("buildSearchableOptions") {
    enabled = false
}
