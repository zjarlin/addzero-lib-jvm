plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") version "2026.02.02"
}
val libs = versionCatalogs.named("libs")

dependencies {
    compileOnly(libs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(libs.findLibrary("org-springframework-spring-context").get())
    api(libs.findLibrary("site-addzero-tool-database-model").get())

    // 添加测试依赖
    testImplementation(libs.findLibrary("org-springframework-spring-jdbc").get())
    testImplementation(libs.findLibrary("mysql-mysql-connector-java").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
    testImplementation(libs.findLibrary("site-addzero-tool-sql-executor").get())
}
