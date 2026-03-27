plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "+"
//    id("kmp-json")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-yaml-snakeyaml").get())
    implementation(libs.findLibrary("cn-hutool-hutool-core").get())

}


