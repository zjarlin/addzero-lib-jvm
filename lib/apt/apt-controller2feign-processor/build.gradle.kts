plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    implementation(libs.javapoet)
    api(libs.lsi.apt)
    api(libs.lsi.core)
    implementation(libs.tool.str)
}
