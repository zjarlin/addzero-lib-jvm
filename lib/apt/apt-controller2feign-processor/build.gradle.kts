plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-squareup-javapoet").get())
    api(libs.findLibrary("site-addzero-lsi-apt").get())
    api(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())
}
