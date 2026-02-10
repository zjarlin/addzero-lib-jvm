plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // Caffeine cache for high-performance caching
    implementation(libs.com.github.ben.manes.caffeine.caffeine)
}
