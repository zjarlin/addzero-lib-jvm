package site.addzero.gradle.plugin


plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.gradle.plugin.kotlin-convention")
}


kotlin {
    sourceSets {
        main {
            val string = "build/generated/ksp/main/kotlin"
            kotlin.srcDir(string)
        }
    }
}
