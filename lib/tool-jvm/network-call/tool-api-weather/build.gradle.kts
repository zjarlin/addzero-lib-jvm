plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // HTTP客户端
//    implementation(libs.com.squareup.okhttp3.okhttp)

    // JSON处理
//    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    // 日志
//    implementation(libs.org.slf4j.slf4j.api)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)

    implementation(libs.org.jsoup.jsoup)
    // 临时添加hutool-http用于测试
    implementation(libs.cn.hutool.hutool.http)

    // SQLite JDBC 驱动
    implementation(libs.org.xerial.sqlite.jdbc)

    // JUnit for testing
//    testImplementation(libs.junit.junit.junit.jupiter)


}
