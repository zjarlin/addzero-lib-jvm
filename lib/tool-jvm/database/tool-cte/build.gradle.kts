plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") version "2026.02.02"
}

dependencies {
    compileOnly(libs.org.springframework.spring.jdbc)
    compileOnly(libs.org.springframework.spring.context)
    api(libs.site.addzero.tool.database.model)

    // 添加测试依赖
    testImplementation(libs.org.springframework.spring.jdbc)
    testImplementation(libs.mysql.mysql.connector.java)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testImplementation(libs.site.addzero.tool.sql.executor)
}
