plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api("site.addzero:apt-dict-trans-core:2025.12.16")
    implementation("com.squareup:javapoet:1.13.0")

    // 基本APT依赖
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    // Spring依赖（用于生成的转换器）

    // 测试时也需要处理器
    testImplementation("org.springframework:spring-beans:5.3.21")
    testImplementation("org.projectlombok:lombok:1.18.38")
    testImplementation("org.springframework:spring-context:5.3.21")
//    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testAnnotationProcessor(project(":lib:apt:dict-trans:apt-dict-trans-processor"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xdiags:verbose")
}

