package site.addzero.spring

import site.addzero.gradle.getLibs

plugins {
    id("site.addzero.spring.spring-common")
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(8))
//    }
//}
//
//kotlin {
//    jvmToolchain(8)
//}

dependencies {
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
//    compileOnly("org.aspectj:aspectjweaver:1.9.9")
    compileOnly("org.aspectj:aspectjweaver")
}
