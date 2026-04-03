plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    // Jakarta Mail API 用于发送邮件
    implementation(catalogLibs.findLibrary("com-sun-mail-jakarta-mail").get())

    // 日志记录
    implementation(catalogLibs.findLibrary("org-slf4j-slf4j-api").get())

    // 测试依赖
    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
    testImplementation(catalogLibs.findLibrary("org-mockito-mockito-core").get())
}
