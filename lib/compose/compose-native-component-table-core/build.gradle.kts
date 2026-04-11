plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
//    id("kmp-shared")

}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib-common"))
        }
    }
}
