
package site.addzero.jvm
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
}

val libs = the<LibrariesForLibs>()

val jdkversion = libs.versions.jdk.get()
extensions.configure<JavaPluginExtension> {
    val toVersion = JavaVersion.toVersion(jdkversion)
    sourceCompatibility = toVersion
    targetCompatibility = toVersion
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkversion.toInt()))
    }
}


