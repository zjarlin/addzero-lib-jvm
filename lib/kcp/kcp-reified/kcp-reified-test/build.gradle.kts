import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

plugins {
    kotlin("jvm")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // 依赖注解模块
    implementation(project(":lib:kcp:kcp-reified:kcp-reified-annotations"))

    // Apply compiler plugin to this module compilation (K2/FIR).
    kotlinCompilerPluginClasspath(project(":lib:kcp:kcp-reified:kcp-reified-plugin"))

    // 测试依赖
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

// 创建一个使用 kotlinc 的自定义编译任务来测试 KCP 插件
tasks.register<Exec>("compileTestWithKcp") {
    group = "build"
    description = "Compile test sources using kotlinc with KCP plugin"

    // 确保依赖项目先构建（仅 assemble，不包含测试）
    dependsOn(":lib:kcp:kcp-reified:kcp-reified-plugin:assemble")
    dependsOn(":lib:kcp:kcp-reified:kcp-reified-annotations:assemble")

    // 配置执行的 kotlinc 命令
    executable = "kotlinc"

    // 先执行 doFirst 来设置参数
    doFirst {
        val pluginJar = project(":lib:kcp:kcp-reified:kcp-reified-plugin").layout.buildDirectory
            .file("libs").get().asFile.listFiles()?.firstOrNull {
                it.name.endsWith(".jar") && !it.name.contains("javadoc") && !it.name.contains("sources")
            }
        val annotationsJar = project(":lib:kcp:kcp-reified:kcp-reified-annotations").layout.buildDirectory
            .file("libs").get().asFile.listFiles()?.firstOrNull {
                it.name.endsWith(".jar") && !it.name.contains("javadoc") && !it.name.contains("sources")
            }

        if (pluginJar == null || !pluginJar.exists()) {
            throw GradleException("Plugin JAR not found")
        }
        if (annotationsJar == null || !annotationsJar.exists()) {
            throw GradleException("Annotations JAR not found")
        }

        val sourceDir = project.projectDir.resolve("src/test/kotlin")
        val outputDir = project.layout.buildDirectory.dir("kcp-compiled").get().asFile

        outputDir.mkdirs()

        val sourceFiles = sourceDir.walk().filter { it.extension == "kt" }.toList()
        if (sourceFiles.isEmpty()) {
            throw GradleException("No Kotlin source files found in $sourceDir")
        }

        val classpath = configurations.testRuntimeClasspath.get().map { it.absolutePath }
            .plus(annotationsJar.absolutePath)
            .plus(pluginJar.absolutePath)
            .joinToString(File.pathSeparator)

        // 设置 kotlinc 参数
        args = listOf(
            "-Xplugin", pluginJar.absolutePath,
            "-cp", classpath,
            "-d", outputDir.absolutePath
        ) + sourceFiles.map { it.absolutePath }

        project.logger.lifecycle("[KCP] Compiling ${sourceFiles.size} files with KCP plugin")
        project.logger.lifecycle("[KCP] Plugin: $pluginJar")
        project.logger.lifecycle("[KCP] Output: $outputDir")
    }
}

println("[Config] Reified KCP plugin configured for ${project.name}")
