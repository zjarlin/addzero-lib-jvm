plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // PostgreSQL JDBC 驱动（可选，根据实际数据库类型调整）
    implementation("org.postgresql:postgresql:42.7.2")

    // MySQL JDBC 驱动（可选）
     implementation("mysql:mysql-connector-java:8.0.33")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
