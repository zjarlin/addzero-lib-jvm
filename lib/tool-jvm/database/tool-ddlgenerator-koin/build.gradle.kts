plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("com.google.devtools.ksp")
}

dependencies {
    // Core module
    api(project(":lib:tool-jvm:database:tool-ddlgenerator-core"))
    
    // All dialects
    api(project(":lib:tool-jvm:database:tool-ddlgenerator-dialect-mysql"))
    api(project(":lib:tool-jvm:database:tool-ddlgenerator-dialect-postgresql"))
    
    // Koin
    api("io.insert-koin:koin-core:3.5.3")
    api("io.insert-koin:koin-annotations:1.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
}
