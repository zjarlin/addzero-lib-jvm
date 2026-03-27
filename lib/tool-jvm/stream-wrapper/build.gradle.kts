plugins {
    id("site.addzero.gradle.plugin.lombok-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
//    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    implementation(libs.findLibrary("com-baomidou-mybatis-plus-core").get())
//    implementation(libs.findLibrary("com-baomidou-mybatis-plus").get())
    implementation(libs.findLibrary("org-apache-commons-commons-lang3").get())

}
