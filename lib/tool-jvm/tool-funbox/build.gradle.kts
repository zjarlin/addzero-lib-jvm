plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.cn.hutool.hutool.all)
//    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    compileOnly(libs.com.alibaba.fastjson2.fastjson2.kotlin)
    implementation(libs.io.swagger.swagger.annotations)

    implementation(libs.site.addzero.tool.reflection)
}
