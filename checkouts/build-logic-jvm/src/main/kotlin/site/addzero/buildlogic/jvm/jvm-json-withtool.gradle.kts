package site.addzero.buildlogic.jvm

import org.gradle.accessors.dm.LibrariesForLibs
plugins {
    id("site.addzero.buildlogic.jvm.jvm-json")
}
val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.tool.json)
}
