plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    // Caffeine cache for high-performance caching
    implementation(libs.findLibrary("com-github-ben-manes-caffeine-caffeine").get())
}
