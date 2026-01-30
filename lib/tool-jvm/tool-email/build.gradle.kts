plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
    // Jakarta Mail API 用于发送邮件
    implementation(libs.jakarta.mail)
    
    // 日志记录
    implementation(libs.slf4j.api)
    
    // 测试依赖
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
}