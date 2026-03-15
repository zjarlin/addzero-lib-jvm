package site.addzero.buildlogic.intellij

import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.intellij.platform.base")
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")
val intellijVersion = libs.findVersion("intellij-idea-version").get().requiredVersion
val intellijJvmTarget = libs.findVersion("intellij-jvm-target").get().requiredVersion

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
    intellijIdeaUltimate(intellijVersion)
    bundledPlugins(
      "com.intellij.java",
      "org.jetbrains.kotlin",
    )
//         添加 SSH 相关插件依赖
//        plugin("com.intellij.ssh")
//        plugin("com.intellij.remote")
  }
}


afterEvaluate {
  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = intellijJvmTarget
    targetCompatibility = intellijJvmTarget
  }
  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.valueOf("JVM_${intellijJvmTarget}"))
    }
  }
}


java {
  sourceCompatibility = JavaVersion.toVersion(intellijJvmTarget.toInt())
  targetCompatibility = JavaVersion.toVersion(intellijJvmTarget.toInt())
}

// 设置 JVM 目标版本为 17 以匹配 IntelliJ 平台
tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.valueOf("JVM_${intellijJvmTarget}"))
  }
}
