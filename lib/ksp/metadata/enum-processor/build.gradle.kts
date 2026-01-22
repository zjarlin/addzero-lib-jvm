plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
//            implementation(libs.kotlinpoet)
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
