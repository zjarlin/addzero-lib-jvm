plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // Jakarta Mail API 用于发送邮件
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    
    // 日志记录
    implementation(libs.slf4j.api)
    
    // 测试依赖
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation("org.mockito:mockito-core:5.7.0")
}