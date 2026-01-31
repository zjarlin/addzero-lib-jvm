plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies{

    implementation(libs.fastexcel)
    implementation(libs.hutool.all)
//    implementation(libs.byte.buddy)
    implementation(libs.byte.buddy)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
}

