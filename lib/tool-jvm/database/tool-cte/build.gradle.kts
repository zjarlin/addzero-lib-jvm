plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
    api("site.addzero:tool-database-model:2025.11.15")
    
    // 添加测试依赖
    testImplementation("org.springframework:spring-jdbc:5.3.21")
    testImplementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("site.addzero:tool-sql-executor:2025.11.26")
}