plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "+"
    id("site.addzero.gradle.plugin.intellij-core")  version "2025.12.23"

}

dependencies {
    implementation(libs.site.addzero.lsi.core)
    implementation(libs.site.addzero.lsi.intellij)
    implementation(libs.site.addzero.tool.str)

}

