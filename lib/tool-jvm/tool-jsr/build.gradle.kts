plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")

    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies{
    implementation(libs.findLibrary("javax-validation-validation-api").get())
    api(libs.findLibrary("site-addzero-tool-context").get())
    api(libs.findLibrary("site-addzero-tool-reflection").get())
//    todo 换成细粒度的
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
//    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
//    implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())

    // 添加Spring相关依赖用于唯一性校验
    compileOnly(libs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(libs.findLibrary("org-springframework-spring-context").get())
}
