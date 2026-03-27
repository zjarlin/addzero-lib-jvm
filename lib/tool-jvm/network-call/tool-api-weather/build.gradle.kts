plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    // HTTP客户端
//    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())

    // JSON处理
//    implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    // 日志
//    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())

    implementation(libs.findLibrary("org-jsoup-jsoup").get())
    // 临时添加hutool-http用于测试
    implementation(libs.findLibrary("cn-hutool-hutool-http").get())

    // SQLite JDBC 驱动
    implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())

    // JUnit for testing
//    testImplementation(libs.junit.junit.junit.jupiter)


}
