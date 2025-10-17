package site.addzero.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

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
 * 1. project属性 "java.springVersion"
 * 2. project属性 "addzero.java.springVersion"
 * 3. gradle.properties中的 "addzero.java.springVersion"
 * 4. 版本目录中的 libs.versions.jdk
 * 5. 默认值（由调用方指定）
 */
fun Project.getJavaVersion(defaultVersion: Int = 8): Int {
    return findProperty("java.springVersion")?.toString()?.toInt()
        ?: findProperty("addzero.java.springVersion")?.toString()?.toInt()
        ?: project.findProperty("addzero.java.springVersion")?.toString()?.toInt()
        ?: try {
            getLibs().versions.jdk.get().toInt()
        } catch (e: Exception) {
            defaultVersion
        }
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
