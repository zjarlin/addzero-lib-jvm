package site.addzero.buildlogic.jvm

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.*

plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}


val libs = the<LibrariesForLibs>()
dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

}
