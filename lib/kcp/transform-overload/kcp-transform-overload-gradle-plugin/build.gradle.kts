import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.util.Properties

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `java-gradle-plugin`
}

dependencies {
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    testImplementation(gradleTestKit())
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

gradlePlugin {
    plugins {
        create("transformOverload") {
            id = "site.addzero.kcp.transform-overload"
            implementationClass = "site.addzero.kcp.transformoverload.gradle.TransformOverloadGradleSubplugin"
        }
    }
}

tasks.processResources {
    dependsOn("generateTransformOverloadPluginCoordinates")
    from(generatedCoordinatesDir)
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/transformOverload/resources")

val generateTransformOverloadPluginCoordinates = tasks.register("generateTransformOverloadPluginCoordinates") {
    val outputFile = generatedCoordinatesDir.map { dir ->
        dir.file("site/addzero/kcp/transformoverload/gradle-plugin.properties")
    }
    inputs.property("groupId", providers.provider { project.group.toString() })
    inputs.property("version", providers.provider { project.version.toString() })
    outputs.file(outputFile)
    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        val properties = Properties().apply {
            setProperty("groupId", project.group.toString())
            setProperty("version", project.version.toString())
        }
        file.writer().use { writer ->
            properties.store(writer, null)
        }
    }
}


val repoRootDir = project.projectDir
    .toPath()
    .resolve("../../../../")
    .normalize()
    .toFile()
val compilerPluginProjectPath = ":lib:kcp:transform-overload:kcp-transform-overload-plugin"
val annotationsProjectPath = ":lib:kcp:transform-overload:kcp-transform-overload-annotations"
val compilerPluginBuildDir = project(compilerPluginProjectPath).layout.buildDirectory
val annotationsBuildDir = project(annotationsProjectPath).layout.buildDirectory

tasks.test {
    dependsOn("$compilerPluginProjectPath:jar", "$annotationsProjectPath:jvmJar")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("transformOverload.repoRoot", repoRootDir.absolutePath)
    systemProperty("transformOverload.pluginGroup", project.group.toString())
    systemProperty("transformOverload.pluginVersion", project.version.toString())
    systemProperty("transformOverload.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("transformOverload.annotationsBuildDir", annotationsBuildDir.get().asFile.absolutePath)
    systemProperty("transformOverload.gradlePluginClasspath", sourceSets.main.get().runtimeClasspath.asPath)
}
