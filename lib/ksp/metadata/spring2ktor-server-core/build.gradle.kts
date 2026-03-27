plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(project.dependencies.platform(libs.io.insert.koin.koin.bom))
    api(catalogLibs.findLibrary("io-insert-koin-koin-core").get())

    api(catalogLibs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(catalogLibs.findLibrary("org-springframework-spring-web").get())
}
