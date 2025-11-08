plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies{
   implementation(libs.hutool.core)
    implementation(libs.hutool.http)
    implementation(libs.fastjson2.kotlin)
//    implementation("site.addzero:tool-str:${libs.versions.addzero.lib.get()}")
//    implementation("site.addzero:tool-jvmstr:${libs.versions.addzero.lib.get()}")
    implementation(projects.lib.toolJvm.toolJvmstr)
    implementation(projects.lib.toolJvm.toolReflection)
}
