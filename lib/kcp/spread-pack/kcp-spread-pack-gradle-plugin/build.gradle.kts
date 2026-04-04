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
        create("spreadPack") {
            id = "site.addzero.kcp.spread-pack"
            implementationClass = "site.addzero.kcp.spreadpack.SpreadPackGradleSubplugin"
        }
    }
}

tasks.processResources {
    dependsOn("generateSpreadPackPluginCoordinates")
    from(generatedCoordinatesDir)
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/spreadPack/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()
val kotlinVersion = libs.findVersion("kotlin").get().requiredVersion

val generateSpreadPackPluginCoordinates = tasks.register<WriteProperties>(
    "generateSpreadPackPluginCoordinates",
) {
    destinationFile = generatedCoordinatesDir
        .map { dir ->
            dir.file("site/addzero/kcp/spreadpack/gradle-plugin.properties")
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
val compilerPluginProjectPath = ":lib:kcp:spread-pack:kcp-spread-pack-plugin"
val annotationsProjectPath = ":lib:kcp:spread-pack:kcp-spread-pack-annotations"
val compilerPluginBuildDir = project(compilerPluginProjectPath).layout.buildDirectory
val annotationsBuildDir = project(annotationsProjectPath).layout.buildDirectory

tasks.test {
    dependsOn(
        "$compilerPluginProjectPath:jar",
        "$annotationsProjectPath:jvmJar",
        "$annotationsProjectPath:allMetadataJar",
        "$annotationsProjectPath:generateMetadataFileForJvmPublication",
        "$annotationsProjectPath:generatePomFileForJvmPublication",
        "$annotationsProjectPath:generateMetadataFileForKotlinMultiplatformPublication",
        "$annotationsProjectPath:generatePomFileForKotlinMultiplatformPublication",
    )
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("spreadPack.repoRoot", repoRootDir.absolutePath)
    systemProperty("spreadPack.pluginGroup", groupId)
    systemProperty("spreadPack.pluginVersion", pluginVersion)
    systemProperty("spreadPack.kotlinVersion", kotlinVersion)
    systemProperty("spreadPack.compilerPluginBuildDir", compilerPluginBuildDir.get().asFile.absolutePath)
    systemProperty("spreadPack.annotationsBuildDir", annotationsBuildDir.get().asFile.absolutePath)
}
