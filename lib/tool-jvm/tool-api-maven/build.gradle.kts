plugins {
    id("site.addzero.jvm.kotlin-convention")
}

dependencies {
    // HTTP客户端
    implementation(libs.okhttp)

    // JSON处理
    implementation(libs.jackson.module.kotlin)
    // 日志
    implementation(libs.slf4j.api)

}
