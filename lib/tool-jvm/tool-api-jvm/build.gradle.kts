plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    kotlin("plugin.serialization")
}

dependencies {
    // OkHttp 核心库
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
}

