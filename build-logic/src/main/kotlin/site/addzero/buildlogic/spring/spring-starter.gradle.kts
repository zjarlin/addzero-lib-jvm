package site.addzero.buildlogic.spring

plugins {
    id("site.addzero.buildlogic.spring.spring-common")
    kotlin("plugin.spring")
}

val libs = versionCatalogs.named("libs")
val springBootVersion = libs.findVersion("spring-boot").get().requiredVersion

dependencies {
    compileOnly(libs.findLibrary("org-springframework-boot-spring-boot-starter-web").get())
    compileOnly(libs.findLibrary("org-springframework-boot-spring-boot-autoconfigure").get())
    annotationProcessor(libs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}
