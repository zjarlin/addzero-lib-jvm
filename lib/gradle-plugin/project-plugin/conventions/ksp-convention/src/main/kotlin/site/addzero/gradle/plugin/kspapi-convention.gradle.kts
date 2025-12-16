package site.addzero.buildlogic.jvm
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.KspConventionExtension

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val extension = the<KspConventionExtension>()

kotlin {
    dependencies {
        implementation("com.google.devtools.ksp:symbol-processing-api:${extension.kspVersion.get()}")
    }
}
