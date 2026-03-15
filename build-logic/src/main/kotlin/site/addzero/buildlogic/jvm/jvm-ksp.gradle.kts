package site.addzero.buildlogic.jvm

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("com.google.devtools.ksp")
//  id("com.fueledbycaffeine.autoservice")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        ksp(libs.findLibrary("dev-zacsweers-autoservice-auto-service-ksp").get())
        // NOTE: It's important that you _don't_ use compileOnly here, as it will fail to resolve at compile-time otherwise
        implementation(libs.findLibrary("com-google-auto-service-annotations").get())

        implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    }
}
