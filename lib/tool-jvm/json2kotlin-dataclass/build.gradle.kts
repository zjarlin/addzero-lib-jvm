plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.com.google.code.gson.gson)
    implementation(libs.site.addzero.tool.jvmstr)
    // JTE Template Engine
//    implementation(libs.gg.jte.jte)
//    implementation(libs.gg.jte.jte.kotlin)
}


description = "JSON to Kotlin Data Class - 生成 data class 定义 + 实例赋值代码"
