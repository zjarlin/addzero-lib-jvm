package site.addzero.gradle

import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * JVM配置工具类 - 提供Java和Kotlin配置的公共函数
 * 仿照RepoUtil.kt的模式，使用Project扩展函数
 */

/**
 * 安全获取版本目录访问器
 */
fun Project.getLibs(): LibrariesForLibs = the<LibrariesForLibs>()

/**
 * 获取项目Java版本
 * 支持多种配置方式：
 * 1. project属性 "java.version"
 * 2. project属性 "addzero.java.version"
 * 3. gradle.properties中的 "addzero.java.version"
 * 4. 版本目录中的 libs.versions.jdk
 * 5. 默认值8
 */
fun Project.getJavaVersion(): Int {
    return findProperty("java.version")?.toString()?.toInt()
        ?: findProperty("addzero.java.version")?.toString()?.toInt()
        ?: project.findProperty("addzero.java.version")?.toString()?.toInt()
        ?: try {
            getLibs().versions.jdk.get().toInt()
        } catch (e: Exception) {
            8
        }
}

/**
 * 根据Java版本获取对应的Kotlin JVM目标版本
 * Java 8使用"1.8"，其他版本使用数字字符串
 */
fun getKotlinJvmTarget(javaVersion: Int): String {
    return if (javaVersion >= 9) javaVersion.toString() else "1.$javaVersion"
}

/**
 * 配置Java版本兼容性
 * 统一设置sourceCompatibility和targetCompatibility
 */
fun Project.configureJavaCompatibility(javaVersion: Int) {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

/**
 * 配置Java工具链
 * 统一设置toolchain语言版本
 */
fun Project.configureJavaToolchain(javaVersion: Int) {
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }
}

/**
 * 完整的Java配置（兼容性 + 工具链）
 */
fun Project.configureJava(javaVersion: Int) {
    configureJavaCompatibility(javaVersion)
    configureJavaToolchain(javaVersion)
}

/**
 * 配置Kotlin编译选项
 * 统一设置编译器参数和JVM目标版本
 */
fun Project.configureKotlinCompilation(
    javaVersion: Int,
    jvmTarget: JvmTarget? = null,
    additionalCompilerArgs: List<String> = emptyList()
) {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            val baseArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            val allArgs = baseArgs + additionalCompilerArgs
            freeCompilerArgs.set(allArgs)

            // 优先使用指定的jvmTarget，否则根据Java版本自动计算
            val targetVersion = jvmTarget ?: JvmTarget.fromTarget(getKotlinJvmTarget(javaVersion))
            this.jvmTarget.set(targetVersion)
        }
    }
}

/**
 * 配置Kotlin工具链
 */
fun Project.configureKotlinToolchain(javaVersion: Int) {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(javaVersion)
    }
}

/**
 * 完整的Kotlin配置（编译选项 + 工具链）
 */
fun Project.configureKotlin(
    javaVersion: Int,
    jvmTarget: JvmTarget? = null,
    additionalCompilerArgs: List<String> = emptyList()
) {
    configureKotlinCompilation(javaVersion, jvmTarget, additionalCompilerArgs)
    configureKotlinToolchain(javaVersion)
}

// 便捷方法：配置特定版本的Java
fun Project.configureJava8() = configureJava(8)
fun Project.configureJava11() = configureJava(11)
fun Project.configureJava17() = configureJava(17)
fun Project.configureJava21() = configureJava(21)
fun Project.configureJava25() = configureJava(25)

// 便捷方法：配置特定版本的Kotlin
fun Project.configureKotlin8(additionalArgs: List<String> = emptyList()) =
    configureKotlin(8, JvmTarget.JVM_1_8, additionalArgs)
fun Project.configureKotlin11(additionalArgs: List<String> = emptyList()) =
    configureKotlin(11, JvmTarget.JVM_11, additionalArgs)
fun Project.configureKotlin17(additionalArgs: List<String> = emptyList()) =
    configureKotlin(17, JvmTarget.JVM_17, additionalArgs)
fun Project.configureKotlin21(additionalArgs: List<String> = emptyList()) =
    configureKotlin(21, JvmTarget.JVM_21, additionalArgs)

/**
 * 完整的Java + Kotlin配置
 * 最常用的配置组合
 */
fun Project.configureJavaAndKotlin(
    javaVersion: Int,
    kotlinJvmTarget: JvmTarget? = null,
    additionalKotlinCompilerArgs: List<String> = emptyList()
) {
    configureJava(javaVersion)
    configureKotlin(javaVersion, kotlinJvmTarget, additionalKotlinCompilerArgs)
}

// 便捷方法：配置特定版本的Java + Kotlin
fun Project.configureJavaAndKotlin8(additionalArgs: List<String> = emptyList()) =
    configureJavaAndKotlin(8, JvmTarget.JVM_1_8, additionalArgs)
fun Project.configureJavaAndKotlin11(additionalArgs: List<String> = emptyList()) =
    configureJavaAndKotlin(11, JvmTarget.JVM_11, additionalArgs)
fun Project.configureJavaAndKotlin17(additionalArgs: List<String> = emptyList()) =
    configureJavaAndKotlin(17, JvmTarget.JVM_17, additionalArgs)
fun Project.configureJavaAndKotlin21(additionalArgs: List<String> = emptyList()) =
    configureJavaAndKotlin(21, JvmTarget.JVM_21, additionalArgs)

/**
 * 配置UTF-8编码
 * 统一处理所有与编码相关的任务配置
 */
fun Project.configureUtf8Encoding() {
    // Java编译任务编码
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    // Java执行任务编码
    tasks.withType<JavaExec>().configureEach {
        // 添加完整的UTF-8编码支持
        jvmArgs("-Dfile.encoding=UTF-8")
        //保证终端cli打印正确
        jvmArgs("-Dsun.stdout.encoding=UTF-8")
        jvmArgs("-Dsun.stderr.encoding=UTF-8")
        jvmArgs("-Dsun.jnu.encoding=UTF-8")
    }

    // Javadoc任务编码
    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }
}

/**
 * 配置测试任务使用JUnit平台
 */
fun Project.configureJUnitPlatform() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

/**
 * 配置Kotlin测试依赖
 */
fun Project.configureKotlinTestDependencies() {
    dependencies.apply {
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}

/**
 * 配置Java插件扩展以包含源码JAR
 */
fun Project.configureWithSourcesJar() {
    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }
}
