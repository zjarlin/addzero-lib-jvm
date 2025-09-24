
plugins {
    id("kotlin-convention")
    id("ksp4jdbc")
    id("ksp4iso")
    id("ksp4projectdir")
    id("ksp4autoddl")
}

dependencies {
    ksp(libs.jimmer.ksp)
//    ksp(projects.lib.toolJvm.jimmer.addzeroJimmerKspAutoddl)

//    ksp(projects.lib.kld.addzeroKldJimmer)

    ksp(projects.lib.ksp.jdbc2metadata.addzeroJdbc2entityProcessor)
    ksp(projects.lib.ksp.metadata.addzeroEntity2isoProcessor)
    ksp(projects.lib.ksp.metadata.entity2form.addzeroEntity2formProcessor)
    ksp(projects.lib.ksp.metadata.addzeroEntity2mcpProcessor)
    implementation(libs.jimmer.sql.kotlin)
    //easy-excel
    implementation(libs.fastexcel)
    //实体表单核心注解
    implementation(projects.lib.ksp.metadata.entity2form.addzeroEntity2formCore)
    implementation(projects.shared)
    implementation(libs.hutool.all)
}
ksp {

    arg("modelPackageName", "site.addzero.model.entity")
}
