plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {


//    implementation(libs.httpclient)


//    implementation(libs.commons.lang3)
//    implementation(libs.jackson.databind)
    // HTTP客户端
//    implementation(libs.jackson.module.kotlin)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.okhttp)

    // JSON处理
//    implementation(libs.jackson.module.kotlin)
    // 日志
//    implementation(libs.slf4j.api)
//    implementation(libs.fastjson2.kotlin)

//    implementation(libs.jsoup)
    // 临时添加hutool-http用于测试
//    implementation(libs.hutool.http)

    // SQLite JDBC 驱动
//    implementation(libs.sqlite.jdbc)



}
