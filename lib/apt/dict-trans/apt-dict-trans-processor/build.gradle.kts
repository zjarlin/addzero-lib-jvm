plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("site-addzero-apt-dict-trans-core").get())
    implementation(libs.findLibrary("com-squareup-javapoet").get())

    // 基本APT依赖
    implementation(libs.findLibrary("javax-annotation-javax-annotation-api").get())
    // Spring依赖（用于生成的转换器）

    // 测试时也需要处理器
    testImplementation(libs.findLibrary("org-springframework-spring-beans").get())
    testImplementation(libs.findLibrary("org-projectlombok-lombok").get())
    testImplementation(libs.findLibrary("org-springframework-spring-context").get())
//    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testAnnotationProcessor(project(":lib:apt:dict-trans:apt-dict-trans-processor"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xdiags:verbose")
}

