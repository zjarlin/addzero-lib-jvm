plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // HTTP客户端
//    implementation(libs.okhttp)

    // JSON处理
//    implementation(libs.jackson.module.kotlin)
    // 日志
//    implementation(libs.slf4j.api)
    implementation(libs.fastjson2.kotlin)

    implementation(libs.jsoup)
    // 临时添加hutool-http用于测试
    implementation(libs.hutool.http)

    // SQLite JDBC 驱动
    implementation(libs.sqlite.jdbc)

    // JUnit for testing
//    testImplementation(libs.junit.jupiter)


}
