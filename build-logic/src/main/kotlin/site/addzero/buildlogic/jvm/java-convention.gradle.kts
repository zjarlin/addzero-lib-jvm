package site.addzero.buildlogic.jvm

plugins {
    `java-library`
    id("site.addzero.buildlogic.common.configureJavaCompatibility")
    id("site.addzero.buildlogic.common.configureJavaToolchain")
    id("site.addzero.buildlogic.common.configureWithSourcesJar")
    id("site.addzero.buildlogic.common.configureJUnitPlatform")
    id("site.addzero.buildlogic.common.configureUtf8Encoding")
}

