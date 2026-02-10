plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
    id("site.addzero.gradle.plugin.dokka-convention")
//    kotlin("plugin.serialization")
}

// 配置 Kotlin 编译器选项以启用新特性

dependencies {
//    implementation(libs.org.jsoup.jsoup)
    // OkHttp 核心库
//    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.cn.hutool.hutool.system)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
    implementation(libs.site.addzero.tool.str)

    // Kotlinx Serialization 用于 JSON 序列化
//    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json.json.json)

    // Kotlin 反射库（用于运行时访问注释）
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
