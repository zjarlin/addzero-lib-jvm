plugins {
    id("kmp-ksp")
    id("kmp-json")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
            }
        }
        jvmMain{
           dependencies {
               implementation(libs.velocity)
//               implementation("org.apache.velocity:velocity-engine-core:2.3")
           }
        }
    }
}
