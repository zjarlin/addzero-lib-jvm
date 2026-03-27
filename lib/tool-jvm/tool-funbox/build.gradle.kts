plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
//    implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    compileOnly(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
    implementation(libs.findLibrary("io-swagger-swagger-annotations").get())

    implementation(libs.findLibrary("site-addzero-tool-reflection").get())
}
