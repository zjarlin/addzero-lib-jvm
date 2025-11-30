plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies{
   implementation(libs.hutool.core)
    implementation(libs.hutool.http)
    implementation(libs.fastjson2.kotlin)
    implementation("site.addzero:tool-str:${libs.versions.addzero.lib1.get()}")
    implementation("site.addzero:tool-jvmstr:${libs.versions.addzero.lib1.get()}")
//    implementation(projects.lib.toolJvm.toolJvmstr)
    implementation("site.addzero:tool-reflection:${libs.versions.addzero.lib.get()}")
//    implementation("site.addzero:addzero-tool-str:2025.09.30")
}
