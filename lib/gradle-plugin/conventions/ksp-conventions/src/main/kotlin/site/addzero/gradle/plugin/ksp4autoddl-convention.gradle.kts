package site.addzero.gradle.plugin

plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.gradle.plugin.ksp4jdbc-convention")
}

ksp {
    arg("sqlSavePath", "db/autoddl")
    arg("dbType", "pg")
    arg("idType", "bigint")
    arg("id", "id")
    arg("createBy", "create_by")
    arg("updateBy", "update_by")
    arg("createTime", "create_time")
    arg("updateTime", "update_time")
    arg("metaJsonSavePath", "db/autoddl/meta")
    arg("metaJsonSaveName", "jimmer_ddlcontext.json")
}
