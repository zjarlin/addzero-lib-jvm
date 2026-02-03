package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij.platform.base")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

repositories {
    mavenCentral()
    intellijPlatform {
        releases()
        marketplace()
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2025.2.3")
        bundledPlugins(
            "com.intellij.java",
            "org.jetbrains.kotlin",
        )
//         添加 SSH 相关插件依赖
//        plugin("com.intellij.ssh")
//        plugin("com.intellij.remote")
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// 设置 JVM 目标版本为 17 以匹配 IntelliJ 平台
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
