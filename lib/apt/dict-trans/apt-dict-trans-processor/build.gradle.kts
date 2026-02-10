plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    api(libs.site.addzero.apt.dict.trans.core)
    implementation(libs.com.squareup.javapoet)

    // 基本APT依赖
    implementation(libs.javax.annotation.javax.annotation.api)
    // Spring依赖（用于生成的转换器）

    // 测试时也需要处理器
    testImplementation(libs.org.springframework.spring.beans)
    testImplementation(libs.org.projectlombok.lombok)
    testImplementation(libs.org.springframework.spring.context)
//    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testAnnotationProcessor(project(":lib:apt:dict-trans:apt-dict-trans-processor"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xdiags:verbose")
}

