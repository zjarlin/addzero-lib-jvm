plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies{

    implementation("cn.idev.excel:fastexcel:1.2.0")
    implementation(libs.hutool.all)
//    implementation(libs.byte.buddy)
    implementation("net.bytebuddy:byte-buddy:1.17.7")
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
}

