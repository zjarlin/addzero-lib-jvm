plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    // 按需引入hutool模块
    implementation(libs.findLibrary("cn-hutool-hutool-core").get())
    implementation(libs.findLibrary("cn-hutool-hutool-extra").get())

    // fastjson2 支持（用于JSON序列化兜底验证）
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())

}
