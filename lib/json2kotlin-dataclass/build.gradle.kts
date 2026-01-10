plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.tool.jvmstr)
    // JTE Template Engine
//    implementation(libs.jte)
//    implementation(libs.jte.kotlin)
}


description = "JSON to Kotlin Data Class - 生成 data class 定义 + 实例赋值代码"
