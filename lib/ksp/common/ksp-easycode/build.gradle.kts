plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}


kotlin {
    dependencies {
        implementation(libs.site.addzero.addzero.ksp.support)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.org.apache.velocity.velocity.engine.core)
        }
    }

}
