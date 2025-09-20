plugins {
    id("com.google.devtools.ksp")
    id("kotlin-convention")
}

kotlin {

    sourceSets {
        main {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
    }

}
