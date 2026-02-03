plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.fastjson2.kotlin)
    implementation(libs.swagger.annotations)

    implementation(libs.tool.reflection)
}
