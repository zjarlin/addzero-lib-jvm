plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
    api("site.addzero:tool-database-model:2025.11.15")

}
