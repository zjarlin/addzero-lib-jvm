package site.addzero.buildlogic.jvm
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.KspConventionExtension

plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val extension = the<KspConventionExtension>()

kotlin {
    sourceSets {
        main {
            val string = "build/generated/ksp/main/kotlin"
            kotlin.srcDir(string)
        }
    }
}
