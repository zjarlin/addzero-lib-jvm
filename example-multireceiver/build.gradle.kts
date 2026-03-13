import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  application
  id("site.addzero.kcp.multireceiver")
}

group = "site.addzero.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
  google()
}

dependencies {
  testImplementation(kotlin("test"))
}

configurations.configureEach {
  resolutionStrategy.dependencySubstitution {
    findProject(":lib:kcp:multireceiver:kcp-multireceiver-annotations")?.let { annotationsProject ->
      substitute(module("site.addzero:kcp-multireceiver-annotations"))
        .using(project(annotationsProject.path))
    }
    findProject(":lib:kcp:multireceiver:kcp-multireceiver-plugin")?.let { pluginProject ->
      substitute(module("site.addzero:kcp-multireceiver-plugin"))
        .using(project(pluginProject.path))
    }
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}

tasks.test {
  useJUnitPlatform()
  javaLauncher.set(
    javaToolchains.launcherFor {
      languageVersion.set(JavaLanguageVersion.of(17))
    },
  )
}

tasks.named<JavaExec>("run") {
  javaLauncher.set(
    javaToolchains.launcherFor {
      languageVersion.set(JavaLanguageVersion.of(17))
    },
  )
}

application {
  mainClass.set("site.addzero.example.MainKt")
}
