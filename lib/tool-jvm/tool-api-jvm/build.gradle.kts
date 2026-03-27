plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    kotlin("plugin.serialization")
}
val libs = versionCatalogs.named("libs")

dependencies {
    // OkHttp 核心库
    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
}

