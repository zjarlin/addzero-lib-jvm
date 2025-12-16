plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("site.addzero:addzero-kcp-annotations:2025.09.29")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")
}
