plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    kotlin("plugin.serialization")
}

dependencies {
    // OkHttp 核心库
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
}

