plugins {
    id("site.addzero.jvm.kotlin-convention")
    id("site.addzero.other.dokka-convention")
}

dependencies {
    // Netty 核心依赖
    implementation("io.netty:netty-all:4.1.104.Final")

    // 日志依赖
    implementation("org.slf4j:slf4j-api:2.0.9")

    // JSON 处理
    implementation(libs.fastjson2.kotlin)

    // 工具类
    implementation(libs.hutool.all)
}