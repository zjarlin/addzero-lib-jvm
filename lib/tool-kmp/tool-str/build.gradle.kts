import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
     id("site.addzero.buildlogic.kmp.kmp-core")
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
            implementation(libs.tool.pinyin)
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
