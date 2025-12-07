plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("com.google.devtools.ksp")
}

dependencies {
    // Core module
    implementation(project(":lib:tool-jvm:database:tool-ddlgenerator-core"))
    
    // Koin annotations
    implementation("io.insert-koin:koin-annotations:1.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
}
