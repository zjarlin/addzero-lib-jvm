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
        create("multireceiver") {
            id = "site.addzero.kcp.multireceiver"
            implementationClass = "site.addzero.kcp.multireceiver.gradle.MultireceiverGradleSubplugin"
        }
    }
}

tasks.processResources {
    dependsOn("generateMultireceiverPluginCoordinates")
    from(generatedCoordinatesDir)
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/multireceiver/resources")
val groupId = project.group.toString()
val version = project.version.toString()

val generateMultireceiverPluginCoordinates = tasks.register<WriteProperties>(
    "generateMultireceiverPluginCoordinates",
) {
    destinationFile = generatedCoordinatesDir
        .map { dir ->
            dir.file("site/addzero/kcp/multireceiver/gradle-plugin.properties")
        }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", version)
}

val repoRootDir = project.projectDir
    .toPath()
    .resolve("../../../../")
    .normalize()
    .toFile()
val compilerPluginProjectPath = ":lib:kcp:multireceiver:kcp-multireceiver-plugin"
val annotationsProjectPath = ":lib:kcp:multireceiver:kcp-multireceiver-annotations"
val compilerPluginBuildDir = project(compilerPluginProjectPath).layout.buildDirectory
val annotationsBuildDir = project(annotationsProjectPath).layout.buildDirectory

tasks.test {
    dependsOn("$compilerPluginProjectPath:jar", "$annotationsProjectPath:jvmJar")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("multireceiver.repoRoot", repoRootDir.absolutePath)
    systemProperty("multireceiver.pluginGroup", groupId)
    systemProperty("multireceiver.pluginVersion", version)
    systemProperty("multireceiver.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("multireceiver.annotationsBuildDir", annotationsBuildDir.get().asFile.absolutePath)
    systemProperty("multireceiver.gradlePluginClasspath", sourceSets.main.get().runtimeClasspath.asPath)
}
