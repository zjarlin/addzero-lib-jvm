import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    id("kmp-component")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {

                implementation(projects.lib.compose.addzeroComposeNativeComponentButton)

            }
        }
    }

}


