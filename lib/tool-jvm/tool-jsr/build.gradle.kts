plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")

    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies{
    implementation("javax.validation:validation-api:2.0.1.Final")
    api(projects.lib.toolJvm.toolContext)
    api(projects.lib.toolJvm.toolReflection)
//    todo 换成细粒度的
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
//    implementation(libs.kotlin.reflect)
//    implementation(libs.jackson.module.kotlin)
    
    // 添加Spring相关依赖用于唯一性校验
    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
    compileOnly(projects.lib.toolJvm.toolSpring)
}