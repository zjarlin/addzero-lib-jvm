plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{
   implementation(libs.cn.hutool.hutool.core)
    implementation(libs.cn.hutool.hutool.http)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
    implementation(libs.site.addzero.tool.str)
    implementation(libs.site.addzero.tool.jvmstr)
//    implementation(libs.site.addzero.tool.jvmstr)
    implementation(libs.site.addzero.tool.reflection)
//    implementation(libs.site.addzero.tool.str)
}
