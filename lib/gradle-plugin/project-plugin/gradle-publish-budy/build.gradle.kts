import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.`kotlin-dsl`
import site.addzero.gradle.tool.configureJdk

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}
val catalogLibs = versionCatalogs.named("libs")

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
    }
}
configureJdk("17")


//version = "2026.03.02"

dependencies {
    gradleApi()
    implementation(catalogLibs.findLibrary("com-vanniktech-gradle-maven-publish-plugin").get())
    implementation(catalogLibs.findLibrary("site-addzero-gradle-script-core").get())
    testImplementation(gradleTestKit())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
    testRuntimeOnly(catalogLibs.findLibrary("org-junit-platform-junit-platform-launcher").get())
}

tasks.test {
    useJUnitPlatform()
}
version = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
