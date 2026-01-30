plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
    // Caffeine cache for high-performance caching
    implementation(libs.caffeine)
}
