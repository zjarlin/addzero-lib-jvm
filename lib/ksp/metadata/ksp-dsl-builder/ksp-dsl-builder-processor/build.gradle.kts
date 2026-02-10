plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.addzero.ksp.dsl.builder.core)

        }
        jvmMain.dependencies {
            implementation(libs.cn.hutool.hutool.all)
        }
    }
}
