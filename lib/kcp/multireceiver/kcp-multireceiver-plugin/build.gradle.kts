plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    implementation(libs.addzero.kcp.annotations)
    implementation(libs.kotlin.compiler.embeddable)
}
