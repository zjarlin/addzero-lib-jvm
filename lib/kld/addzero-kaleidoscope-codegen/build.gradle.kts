plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
//                implementation(project(":lib:kld:addzero-kaleidoscope-ksp"))
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
