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
               implementation("org.apache.velocity:velocity:1.7")
//               implementation("org.apache.velocity:velocity-engine-core:2.3")
           }
        }
    }
}
