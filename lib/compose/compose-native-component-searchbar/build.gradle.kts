import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {

                implementation(projects.lib.compose.composeNativeComponentButton)

            }
        }
    }

}


