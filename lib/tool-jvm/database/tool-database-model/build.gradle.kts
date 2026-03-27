plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("kmp-json")
}
val libs = versionCatalogs.named("libs")

dependencies {
    // 添加Spring JDBC依赖用于数据库操作
    compileOnly(libs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(libs.findLibrary("org-springframework-spring-context").get())
//    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
}
