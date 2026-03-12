import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.JavaExec

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
