package site.addzero.gradle.plugin

import com.codingfeline.buildkonfig.compiler.FieldSpec

buildscript {
    dependencies {
        classpath("site.addzero.gradle:gradle-tool")
    }
}

plugins {
    id("com.codingfeline.buildkonfig")
}

buildkonfig {
    packageName = "site.addzero"

    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.STRING, "test", "aaa")
    }
}
