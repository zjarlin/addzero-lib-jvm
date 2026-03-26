plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
  id("site.addzero.gradle.plugin.processor-buddy")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.site.addzero.route.core)
//            implementation(libs.site.addzero.ksp.support)
        }
    }
}
