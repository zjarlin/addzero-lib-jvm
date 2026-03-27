plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("cn-hutool-hutool-core").get())
    implementation(libs.findLibrary("com-baomidou-mybatis-plus-core").get())
//    implementation(libs.findLibrary("com-baomidou-mybatis-plus").get())
//    implementation(libs.findLibrary("org-apache-commons-commons-lang3").get())
//    api(libs.findLibrary("site-addzero-mybatis-auto-wrapper-core").get())


    api(libs.findLibrary("site-addzero-mybatis-auto-wrapper-core").get())
//    implementation(libs.findLibrary("org-springframework-spring-expression").get())

    // SpEL 表达式支持（保持兼容 JDK 8）
    implementation(libs.findLibrary("org-springframework-spring-expression").get())
    implementation(libs.findLibrary("site-addzero-tool-spel").get())
//    implementation(libs.findLibrary("site-addzero-tool-spel").get())
//    implementation(libs.findLibrary("org-springframework-spring-core").get())
}
//implementation(libs.findLibrary("org-mybatis-spring-boot-mybatis-spring-boot-starter").get())
