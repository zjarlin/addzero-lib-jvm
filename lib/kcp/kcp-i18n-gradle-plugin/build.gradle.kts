import org.gradle.api.tasks.WriteProperties
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `java-gradle-plugin`
}

group = "site.addzero"

dependencies {
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    testImplementation(gradleTestKit())
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

gradlePlugin {
    plugins {
        create("i18n") {
            id = "site.addzero.kcp.i18n"
            implementationClass = "site.addzero.kcp.i18n.gradle.I18NGradleSubplugin"
        }
    }
}

tasks.processResources {
    dependsOn("generateI18NPluginCoordinates")
    from(generatedCoordinatesDir)
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/i18n/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateI18NPluginCoordinates = tasks.register<WriteProperties>(
    "generateI18NPluginCoordinates",
) {
    destinationFile = generatedCoordinatesDir
        .map { dir ->
            dir.file("site/addzero/kcp/i18n/gradle-plugin.properties")
        }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

val repoRootDir = project.projectDir
    .toPath()
    .resolve("../../../../")
    .normalize()
    .toFile()
val compilerPluginProjectPath = ":lib:kcp:kcp-i18n"
val runtimeProjectPath = ":lib:kcp:kcp-i18n-runtime"
val compilerPluginBuildDir = project(compilerPluginProjectPath).layout.buildDirectory
val runtimeBuildDir = project(runtimeProjectPath).layout.buildDirectory

tasks.test {
    dependsOn("$compilerPluginProjectPath:jar", "$runtimeProjectPath:jar")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("i18n.repoRoot", repoRootDir.absolutePath)
    systemProperty("i18n.pluginGroup", groupId)
    systemProperty("i18n.pluginVersion", pluginVersion)
    systemProperty("i18n.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("i18n.runtimeBuildDir", runtimeBuildDir.get().asFile.absolutePath)
    systemProperty("i18n.gradlePluginBuildDir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("i18n.gradlePluginClasspath", sourceSets.main.get().runtimeClasspath.asPath)
}
