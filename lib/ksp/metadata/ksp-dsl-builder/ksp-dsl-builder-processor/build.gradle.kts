plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {

  sourceSets {
    commonMain.dependencies {
      implementation(libs.site.addzero.ksp.dsl.builder.core)

    }
    jvmMain.dependencies {

      implementation(project(":lib:lsi:lsi-ksp"))
      implementation(libs.cn.hutool.hutool.all)
    }
  }
}
