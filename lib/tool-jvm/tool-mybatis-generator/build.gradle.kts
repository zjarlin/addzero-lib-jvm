plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
    id("site.addzero.gradle.plugin.graalvm-convention")
    application
}

dependencies {
    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.baomidou.mybatis.plus.generator)
    implementation(libs.com.baomidou.mybatis.plus.annotation)
    // 添加MyBatis Plus核心依赖，解决StringUtils类缺失问题
    implementation(libs.com.baomidou.mybatis.plus.core)
    implementation(libs.org.apache.velocity.velocity.engine.core)
    implementation(libs.mysql.mysql.connector.java)
    implementation(libs.site.addzero.tool.yml)
    implementation(libs.site.addzero.tool.cli.repl)
}

application {
    mainClass.set("site.addzero.lib_adaptor.MpGenCliKt")
}
