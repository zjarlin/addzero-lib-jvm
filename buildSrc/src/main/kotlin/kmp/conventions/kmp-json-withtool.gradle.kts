plugins {
    id("kmp-json")

}
kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:tool-kmp:addzero-kotlinx-serialization-ext"))

        }
    }

}
