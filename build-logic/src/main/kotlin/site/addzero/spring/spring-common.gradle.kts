package site.addzero.spring

import org.gradle.api.provider.Property

// 默认配置
private val springBootVersion = "2.7.18"
private val springBom = "org.springframework.boot:spring-boot-dependencies"

interface SpringCommonExtension {
    val version: Property<String>
}

val create = extensions.create<SpringCommonExtension>("addzeroSpringBuddy").apply {
    version.set(springBootVersion)
}

// 计算属性
val version: String get() = create.version.get()

plugins {
    id("site.addzero.jvm.kotlin-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(platform("$springBom:$version"))
}


