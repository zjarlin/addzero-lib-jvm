plugins {
    id("kotlin-convention")
}

dependencies {
    // Velocity模板引擎
    implementation("org.apache.velocity:velocity-engine-core:2.3")

    // 日志依赖
    implementation("org.slf4j:slf4j-api:2.0.17")

    // KSP相关依赖
//    api("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.14")
       api(libs.ksp.symbol.processing.api)


    // 通用工具类
    implementation(projects.lib.ksp.common.addzeroKspSupport)

    // 测试依赖
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}
