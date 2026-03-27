plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("kmp-json")
}
val libs = versionCatalogs.named("libs")

dependencies {
        implementation(libs.findLibrary("org-tomlj-tomlj").get())
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())


}

