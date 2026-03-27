plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    id("kmp-json")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
//    implementation(libs.findLibrary("site-addzero-tool-str").get())
    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}
