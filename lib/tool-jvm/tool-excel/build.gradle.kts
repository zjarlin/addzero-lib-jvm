plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{

    implementation(libs.fastexcel)
    implementation(libs.hutool.all)
//    implementation(libs.byte.buddy)
    implementation(libs.byte.buddy)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
}

