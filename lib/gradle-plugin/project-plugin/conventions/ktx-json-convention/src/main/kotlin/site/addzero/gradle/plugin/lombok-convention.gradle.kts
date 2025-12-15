package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.*

plugins {
    id("site.addzero.gradle.plugin.java-convention") version "2025-12-16"
}


val libs = the<LibrariesForLibs>()
dependencies {
    implementation(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)

}
