plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}


kotlin {
    dependencies {
        implementation(libs.site.addzero.ksp.support)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.org.apache.velocity.velocity.engine.core)
        }
    }

}
