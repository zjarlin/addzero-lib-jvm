plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
//                implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
                implementation(projects.lib.kld.addzeroKaleidoscopeKsp)
                implementation(projects.lib.kld.addzeroKaleidoscopeCodegen)
            }
        }
        jvmMain{
           dependencies {
               implementation(libs.hutool.all)
//               implementation(libs.kotlin.reflect)
               // 添加对Kaleidoscope APT模块的依赖
//               implementation(projects.lib.kld.addzeroKaleidoscopeApt)
//               implementation("org.apache.velocity:velocity-engine-core:2.3")
           }
        }
    }
}
