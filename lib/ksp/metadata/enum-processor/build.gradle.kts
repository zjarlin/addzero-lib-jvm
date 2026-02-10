plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
//            implementation(libs.com.squareup.kotlinpoet)
            implementation(libs.com.squareup.kotlinpoet.ksp)
        }
    }
}
