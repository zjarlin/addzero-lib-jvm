package site.addzero.buildlogic.kmp.libs

import com.codingfeline.buildkonfig.compiler.FieldSpec
//import site.addzero.gradle.tool.defByClass
//import site.addzero.gradle.tool.defByMap

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
//    defaultConfigs {
//        defByClass(BuildSettings::class)
//    }

    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.STRING, "test", "aaa")
    }


}
