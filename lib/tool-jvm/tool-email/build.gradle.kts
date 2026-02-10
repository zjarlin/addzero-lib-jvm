plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // Jakarta Mail API 用于发送邮件
    implementation(libs.com.sun.mail.jakarta.mail)

    // 日志记录
    implementation(libs.org.slf4j.slf4j.api)

    // 测试依赖
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
    testImplementation(libs.junit.junit.junit.jupiter)
    testImplementation(libs.org.mockito.mockito.core)
}
