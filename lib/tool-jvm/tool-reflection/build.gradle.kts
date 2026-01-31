plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    // 按需引入hutool模块
    implementation(libs.hutool.core)
    implementation(libs.hutool.extra)

    // fastjson2 支持（用于JSON序列化兜底验证）
    implementation(libs.fastjson2.kotlin)
    
}
