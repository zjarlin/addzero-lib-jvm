plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("site.addzero.gradle.plugin.dokka-convention")
}

dependencies {
    // Netty 核心依赖
    implementation(libs.netty.all)

    // 日志依赖
    implementation(libs.slf4j.api)

    // JSON 处理
    implementation(libs.fastjson2.kotlin)

    // 工具类
    implementation(libs.hutool.all)
}
