plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.ksp.support)
            implementation(libs.androidx.room.compiler.processing)
            implementation(libs.site.addzero.entity2form.core)
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}
