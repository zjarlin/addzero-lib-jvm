plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-google-code-gson-gson").get())
    implementation(libs.findLibrary("site-addzero-tool-jvmstr").get())
    // JTE Template Engine
//    implementation(libs.findLibrary("gg-jte-jte").get())
//    implementation(libs.findLibrary("gg-jte-jte-kotlin").get())
}


description = "JSON to Kotlin Data Class - 生成 data class 定义 + 实例赋值代码"
