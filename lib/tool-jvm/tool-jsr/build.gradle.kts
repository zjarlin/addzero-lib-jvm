plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")

    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{
    implementation(libs.javax.validation.validation.api)
    api(libs.site.addzero.tool.context)
    api(libs.site.addzero.tool.reflection)
//    todo 换成细粒度的
    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
//    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
//    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)

    // 添加Spring相关依赖用于唯一性校验
    compileOnly(libs.org.springframework.spring.jdbc)
    compileOnly(libs.org.springframework.spring.context)
}
