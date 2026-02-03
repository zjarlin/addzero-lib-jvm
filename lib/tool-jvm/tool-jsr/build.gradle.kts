plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")

    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies{
    implementation(libs.validation.api)
    api(libs.tool.context)
    api(libs.tool.reflection)
//    todo 换成细粒度的
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
//    implementation(libs.kotlin.reflect)
//    implementation(libs.jackson.module.kotlin)

    // 添加Spring相关依赖用于唯一性校验
    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
}
