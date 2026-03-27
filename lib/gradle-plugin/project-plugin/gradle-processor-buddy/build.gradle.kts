import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`kotlin-dsl`
import site.addzero.gradle.tool.configureJdk

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath(libs.site.addzero.gradle.tool.config.java)
  }
}
configureJdk("8")

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}
val catalogLibs = versionCatalogs.named("libs")

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  gradleApi()
  implementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-gradle-plugin").get())
  implementation(catalogLibs.findLibrary("site-addzero-tool-str").get())
  implementation(catalogLibs.findLibrary("cn-hutool-hutool-core").get())
  testImplementation(kotlin("test"))
}
