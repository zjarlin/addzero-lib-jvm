plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {


//    implementation("org.apache.httpcomponents:httpclient:4.5.14")


//    implementation("org.apache.commons:commons-lang3:3.19.0")
//    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    // HTTP客户端
//    implementation(libs.jackson.module.kotlin)
    implementation(libs.fastjson2.kotlin)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON处理
//    implementation(libs.jackson.module.kotlin)
    // 日志
//    implementation(libs.slf4j.api)
//    implementation(libs.fastjson2.kotlin)

//    implementation("org.jsoup:jsoup:1.15.3")
    // 临时添加hutool-http用于测试
//    implementation(libs.hutool.http)

    // SQLite JDBC 驱动
//    implementation("org.xerial:sqlite-jdbc:3.42.0.0")



}
