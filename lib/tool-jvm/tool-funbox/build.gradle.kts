plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    implementation(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.fastjson2.kotlin)
    implementation(libs.swagger.annotations)

    implementation(libs.tool.reflection)
}
