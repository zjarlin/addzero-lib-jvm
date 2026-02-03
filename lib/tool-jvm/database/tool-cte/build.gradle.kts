plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") version "2026.02.02"
}

dependencies {
    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
    api(libs.tool.database.model)

    // 添加测试依赖
    testImplementation(libs.spring.jdbc)
    testImplementation(libs.mysql.connector.java)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.tool.sql.executor)
}
