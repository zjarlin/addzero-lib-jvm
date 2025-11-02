plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(project(":lib:kcp:kcp-annotations"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")
}
