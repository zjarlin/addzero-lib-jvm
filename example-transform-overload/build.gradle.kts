import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  application
  id("site.addzero.kcp.transform-overload")
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

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  // This example consumes fixed-version artifacts from mavenLocal while we iteratively
  // republish the plugin during development, so reusing stale compile outputs is misleading.
  outputs.upToDateWhen { false }
  outputs.cacheIf { false }
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
