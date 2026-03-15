package site.addzero.buildlogic.kmp

import com.codingfeline.buildkonfig.compiler.FieldSpec
import site.addzero.gradle.tool.loadEnv

plugins {
  id("com.codingfeline.buildkonfig")
}

buildkonfig {
  packageName = "site.addzero"
//    defaultConfigs {
//        defByClass(BuildSettings::class)
//    }
  val loadEnv = loadEnv()

  defaultConfigs() {
    loadEnv.forEach { (k, v) ->
      buildConfigField(FieldSpec.Type.STRING, k, v)

    }
  }
}
