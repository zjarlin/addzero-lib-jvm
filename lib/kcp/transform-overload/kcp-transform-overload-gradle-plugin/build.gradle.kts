import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.WriteProperties

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `java-gradle-plugin`
}
val libs = versionCatalogs.named("libs")

group = "site.addzero"

dependencies {
    implementation(gradleApi())
    implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-gradle-plugin").get())
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
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
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateTransformOverloadPluginCoordinates = tasks.register<WriteProperties>(
    "generateTransformOverloadPluginCoordinates",
) {
    destinationFile = generatedCoordinatesDir
        .map { dir ->
            dir.file("site/addzero/kcp/transformoverload/gradle-plugin.properties")
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
    systemProperty("transformOverload.pluginGroup", groupId)
    systemProperty("transformOverload.pluginVersion", pluginVersion)
    systemProperty("transformOverload.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("transformOverload.annotationsBuildDir", annotationsBuildDir.get().asFile.absolutePath)
    systemProperty("transformOverload.gradlePluginClasspath", sourceSets.main.get().runtimeClasspath.asPath)
}
