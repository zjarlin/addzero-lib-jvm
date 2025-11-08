plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}
dependencies {
    implementation(libs.hutool.all)
    implementation("com.baomidou:mybatis-plus-core:${libs.versions.mybatisPlus.get()}")
//    implementation("com.baomidou:mybatis-plus:3.5.12")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    api(projects.lib.toolJvm.database.mybatisAutoWrapperCore)
}
//implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
