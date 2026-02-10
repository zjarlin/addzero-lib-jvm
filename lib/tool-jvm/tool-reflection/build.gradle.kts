plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // 按需引入hutool模块
    implementation(libs.cn.hutool.hutool.core)
    implementation(libs.cn.hutool.hutool.extra)

    // fastjson2 支持（用于JSON序列化兜底验证）
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)

}
