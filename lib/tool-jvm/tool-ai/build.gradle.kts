plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{
   implementation(libs.hutool.core)
    implementation(libs.hutool.http)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.tool.str)
    implementation(libs.tool.jvmstr)
//    implementation(libs.tool.jvmstr)
    implementation(libs.tool.reflection)
//    implementation(libs.tool.str)
}
