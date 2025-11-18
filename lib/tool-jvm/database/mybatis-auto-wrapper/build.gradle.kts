plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
    implementation(libs.hutool.core)
    implementation("com.baomidou:mybatis-plus-core:${libs.versions.mybatisPlus.get()}")
//    implementation("com.baomidou:mybatis-plus:3.5.12")
//    implementation("org.apache.commons:commons-lang3:3.18.0")
    api(projects.lib.toolJvm.database.mybatisAutoWrapperCore)


    // SpEL 表达式支持（保持兼容 JDK 8）
    implementation("org.springframework:spring-expression:5.3.39")
    implementation("site.addzero:tool-spel:2025.11.19")
//    implementation(projects.lib.toolJvm.toolSpel)
//    implementation("org.springframework:spring-core:6.1.14")
}
//implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
