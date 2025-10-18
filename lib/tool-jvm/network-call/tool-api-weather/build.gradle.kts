plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // HTTP客户端
    implementation(libs.okhttp)

    // JSON处理
//    implementation(libs.jackson.module.kotlin)
    // 日志
//    implementation(libs.slf4j.api)
    implementation(libs.fastjson2.kotlin)

    implementation("org.jsoup:jsoup:1.15.3")
    // 临时添加hutool-http用于测试
    implementation(libs.hutool.http)
}
