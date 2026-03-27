plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies{

    implementation(catalogLibs.findLibrary("cn-idev-excel-fastexcel").get())
    implementation(catalogLibs.findLibrary("cn-hutool-hutool-all").get())
//    implementation(libs.net.bytebuddy.byte.buddy)
    implementation(libs.net.bytebuddy.byte.buddy)
    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation(catalogLibs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
}

