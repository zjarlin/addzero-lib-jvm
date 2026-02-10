plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{

    implementation(libs.cn.idev.excel.fastexcel)
    implementation(libs.cn.hutool.hutool.all)
//    implementation(libs.net.bytebuddy.byte.buddy)
    implementation(libs.net.bytebuddy.byte.buddy)
    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
}

