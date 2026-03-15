package site.addzero.buildlogic.jvm

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("io.insert-koin.compiler.plugin")
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(libs.findLibrary("io-insert-koin-koin-test").get())

    implementation(project.dependencies.platform(libs.findLibrary("io-insert-koin-koin-bom").get()))
    implementation(libs.findLibrary("io-insert-koin-koin-annotations").get())

    implementation(libs.findLibrary("io-insert-koin-koin-core").get())
    implementation(libs.findLibrary("io-insert-koin-koin-logger-slf4j").get())
}

koinCompiler {
    userLogs = true        // Log component detection
    debugLogs = false      // Verbose logs (off by default)
    dslSafetyChecks = true // Validate DSL usage
}
