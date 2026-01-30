plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
    implementation(libs.addzero.kcp.annotations)
    implementation(libs.kotlin.compiler.embeddable)
}


