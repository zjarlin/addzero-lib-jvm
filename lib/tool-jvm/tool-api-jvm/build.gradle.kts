plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    kotlin("plugin.serialization")
}

dependencies {
    // OkHttp 核心库
    implementation(libs.okhttp)
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
}

