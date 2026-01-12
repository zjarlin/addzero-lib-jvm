plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"

    id("site.addzero.gradle.plugin.intellij-core")  version "2025.12.23"

}

dependencies {
    implementation(libs.lsi.core)
    implementation(libs.tool.str)
    implementation(libs.lsi.intellij)
}
