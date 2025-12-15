plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:apt:dict-trans:apt-dict-trans-core"))
    implementation("com.squareup:javapoet:1.13.0")

    // 基本APT依赖
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
    // Spring依赖（用于生成的转换器）
    compileOnly("org.springframework:spring-context:5.3.21")
    compileOnly("org.springframework:spring-beans:5.3.21")
    
    // 测试时也需要处理器
    testAnnotationProcessor(project(":lib:apt:dict-trans:apt-dict-trans-processor"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xdiags:verbose")
}

