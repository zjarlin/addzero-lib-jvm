plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    kotlin("kapt") // 添加kapt插件用于APT处理器
}

// Fix JVM target compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    // APT 相关依赖 - 用于Java APT处理器
    implementation("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")
    
    // KSP 相关依赖 - 用于Kotlin符号处理
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.14")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    
    // Kotlin reflection support
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
    
    // JavaPoet for Java code generation
    implementation("com.squareup:javapoet:1.13.0")
    
    // Simple string template for code generation (replacing JTE for Java 11 compatibility)
    
    // 工具库
    implementation(libs.hutool.core)
    
    // 引用核心注解模块
    api(project(":lib:apt:dict-trans:apt-dict-annotations"))
    
    // 引用现有的字典翻译核心包
    // api(project(":lib:tool-starter:dict-trans-core")) // Temporarily commented out for testing
    
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}