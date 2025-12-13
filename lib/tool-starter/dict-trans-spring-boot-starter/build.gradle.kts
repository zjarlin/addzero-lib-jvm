
plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
    // AOP 支持
    compileOnly("org.aspectj:aspectjweaver")

    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation("site.addzero:tool-reflection:2025.10.20")
    implementation(libs.byte.buddy)
    implementation(libs.slf4j.api)
    api("site.addzero:dict-trans-core:2025.10.20")
    // 缓存依赖
    implementation(libs.caffeine)

}
