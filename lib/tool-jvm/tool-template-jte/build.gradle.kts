plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    // JTE Template Engine
    implementation(catalogLibs.findLibrary("gg-jte-jte").get())

    // 测试依赖
    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.junit.junit.junit.jupiter)
}

description = "JTE Template Engine utilities"
