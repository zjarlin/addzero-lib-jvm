
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
//    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    compileOnly(libs.findLibrary("com-baomidou-mybatis-plus").get())
    implementation(libs.findLibrary("site-addzero-mybatis-auto-wrapper").get())
//    implementation(libs.findLibrary("site-addzero-mybatis-auto-wrapper").get())
//    implementation(libs.findLibrary("site-addzero-tool-spring").get())
    implementation(libs.findLibrary("cn-hutool-hutool-core").get())
    implementation(libs.findLibrary("site-addzero-tool-spctx").get())
//    compileOnly(projects.)
//    implementation(libs.findLibrary("com-baomidou-mybatis-plus-core").get())
//    implementation(libs.findLibrary("org-apache-commons-commons-lang3").get())
}
//implementation(libs.findLibrary("org-mybatis-spring-boot-mybatis-spring-boot-starter").get())
