plugins {
    kotlin("jvm")
}


dependencies {
    // 核心Kaleidoscope规范
    api(projects.lib.kld.addzeroKaleidoscopeSpec)

    // APT相关依赖
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("com.google.auto.service:auto-service:1.0.1")

    // Kotlin 标准库
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

}


