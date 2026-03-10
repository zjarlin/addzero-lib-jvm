import org.gradle.jvm.toolchain.JavaLanguageVersion

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
    filesMatching("site/addzero/kcp/transformoverload/gradle-plugin.properties") {
        expand(
            "groupId" to project.group.toString(),
            "version" to project.version.toString(),
        )
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
