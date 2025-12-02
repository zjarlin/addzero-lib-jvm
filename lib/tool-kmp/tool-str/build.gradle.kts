import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-core")
}

kotlin{
    jvmToolchain(8)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    sourceSets {
        jvmMain . dependencies {
            implementation("site.addzero:tool-pinyin:2025.10.07")
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
