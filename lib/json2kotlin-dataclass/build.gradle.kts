plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("site.addzero:tool-jvmstr:2025.11.27")
    // JTE Template Engine
//    implementation("gg.jte:jte:3.1.12")
//    implementation("gg.jte:jte-kotlin:3.1.12")
}


description = "JSON to Kotlin Data Class - 生成 data class 定义 + 实例赋值代码"
