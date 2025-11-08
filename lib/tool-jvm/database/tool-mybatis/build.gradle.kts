
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
//    implementation(libs.hutool.all)
    compileOnly("com.baomidou:mybatis-plus:${libs.versions.mybatisPlus.get()}")
    implementation("site.addzero:mybatis-auto-wrapper:${libs.versions.mybatisAutoWrapper.get()}")
//    implementation("site.addzero:mybatis-auto-wrapper:+")
    implementation(projects.lib.toolJvm.toolSpring)
    implementation(libs.hutool.core)
//    compileOnly(projects.)
//    implementation("com.baomidou:mybatis-plus-core:${libs.versions.mybatisPlus.get()}")
//    implementation("org.apache.commons:commons-lang3:3.18.0")
}
//implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
