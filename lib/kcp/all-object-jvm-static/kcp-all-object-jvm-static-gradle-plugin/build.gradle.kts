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
        create("allObjectJvmStatic") {
            id = "site.addzero.kcp.all-object-jvm-static"
            implementationClass = "site.addzero.kcp.allobjectjvmstatic.gradle.AllObjectJvmStaticGradleSubplugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/allObjectJvmStatic/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateAllObjectJvmStaticPluginCoordinates = tasks.register<WriteProperties>(
    "generateAllObjectJvmStaticPluginCoordinates",
) {
    destinationFile = generatedCoordinatesDir
        .map { dir ->
            dir.file("site/addzero/kcp/allobjectjvmstatic/gradle-plugin.properties")
        }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateAllObjectJvmStaticPluginCoordinates)
    from(generatedCoordinatesDir)
}

val repoRootDir = project.projectDir
    .toPath()
    .resolve("../../../../")
    .normalize()
    .toFile()
val compilerPluginProjectPath = ":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin"
val compilerPluginBuildDir = project(compilerPluginProjectPath).layout.buildDirectory

tasks.test {
    dependsOn("$compilerPluginProjectPath:jar")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("allObjectJvmStatic.repoRoot", repoRootDir.absolutePath)
    systemProperty("allObjectJvmStatic.pluginGroup", groupId)
    systemProperty("allObjectJvmStatic.pluginVersion", pluginVersion)
    systemProperty("allObjectJvmStatic.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("allObjectJvmStatic.gradlePluginClasspath", sourceSets.main.get().runtimeClasspath.asPath)
}
