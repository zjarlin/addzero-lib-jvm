plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("site.addzero.gradle.plugin.dokka-convention")
}

dependencies {
    // Netty 核心依赖
    implementation(libs.io.netty.netty.all)

    // 日志依赖
    implementation(libs.org.slf4j.slf4j.api)

    // JSON 处理
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)

    // 工具类
    implementation(libs.cn.hutool.hutool.all)
}
