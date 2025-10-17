package site.addzero.buildlogic.spring

plugins {
    id("site.addzero.buildlogic.spring.spring-common")
}

dependencies {
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-context")

    // 自动配置
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    // 元数据生成
//    compileOnly("org.springframework.boot:spring-boot-autoconfigure-processor")
//    compileOnly("org.aspectj:aspectjweaver:1.9.9")
    compileOnly("org.aspectj:aspectjweaver")
}
