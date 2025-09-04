plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":lib:kcp:addzero-kcp-annotations"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")
}

kotlin {
    jvmToolchain(17)
} 