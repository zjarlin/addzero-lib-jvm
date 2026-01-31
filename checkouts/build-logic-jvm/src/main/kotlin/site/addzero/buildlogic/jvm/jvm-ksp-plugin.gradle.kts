package site.addzero.buildlogic.jvm


plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

kotlin {
    sourceSets {
        main {
            val string = "build/generated/ksp/main/kotlin"
            kotlin.srcDir(string)
        }
    }

}
