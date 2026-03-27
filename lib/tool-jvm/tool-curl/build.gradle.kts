plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-google-code-gson-gson").get())
    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())

}
