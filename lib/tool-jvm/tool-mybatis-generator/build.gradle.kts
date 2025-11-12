plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.graalvm-convention")
    application
}

dependencies {
    implementation(libs.hutool.all)
    implementation(libs.mybatis.plus.generator)
    implementation(libs.mybatis.plus.annotation)
    // 添加MyBatis Plus核心依赖，解决StringUtils类缺失问题
    implementation("com.baomidou:mybatis-plus-core:3.5.3.1")
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("site.addzero:tool-tool-yml:+")
    implementation("site.addzero:tool-cli-repl:+")
}

application {
    mainClass.set("site.addzero.lib_adaptor.MpGenCliKt")
}
