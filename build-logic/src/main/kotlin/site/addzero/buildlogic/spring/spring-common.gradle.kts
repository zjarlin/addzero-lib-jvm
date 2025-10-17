package site.addzero.buildlogic.spring


plugins {
    id("site.addzero.buildlogic.common.ext")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}
val value = the<site.addzero.gradle.AdzeroExtension>()

dependencies {
    //    val version1 = "2.7.5"
    val version = value.springVersion.get()
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$version"))
}


