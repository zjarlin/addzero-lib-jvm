plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("site.addzero.gradle.plugin.dokka-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    // Netty 核心依赖
    implementation(libs.findLibrary("io-netty-netty-all").get())

    // 日志依赖
    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())

    // JSON 处理
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())

    // 工具类
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
}
