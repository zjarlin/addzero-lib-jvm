plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
    id("site.addzero.gradle.plugin.dokka-convention")
//    kotlin("plugin.serialization")
}
val libs = versionCatalogs.named("libs")

// 配置 Kotlin 编译器选项以启用新特性

dependencies {
//    implementation(libs.findLibrary("org-jsoup-jsoup").get())
    // OkHttp 核心库
//    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())
    implementation(libs.findLibrary("cn-hutool-hutool-system").get())
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())

    // Kotlinx Serialization 用于 JSON 序列化
//    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json.json.json)

    // Kotlin 反射库（用于运行时访问注释）
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
