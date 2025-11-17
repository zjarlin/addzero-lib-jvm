plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    id("kmp-json")
}

dependencies {
    // 添加Spring JDBC依赖用于数据库操作
    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
//    implementation(libs.hutool.all)
    api("site.addzero:tool-database-model:2025.11.15")
}
