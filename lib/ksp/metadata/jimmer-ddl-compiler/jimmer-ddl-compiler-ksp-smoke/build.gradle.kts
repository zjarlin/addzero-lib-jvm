plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
}

val catalogLibs = versionCatalogs.named("libs")
val generatedDdlDir = layout.buildDirectory.dir("generated/jimmer-ddl/main/resources/db/migration")
val generatedDdlFilePath = generatedDdlDir.get().file("V9001__ksp_smoke.sql").asFile.absolutePath

dependencies {
    implementation(catalogLibs.findLibrary("org-babyfish-jimmer-jimmer-core").get())
    ksp(project(":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-processor"))
}

ksp {
    arg("jimmerDdl.databaseType", "h2")
    arg("jimmerDdl.outputFormat", "flyway")
    arg("jimmerDdl.outputDir", generatedDdlDir.get().asFile.absolutePath)
    arg("jimmerDdl.version", "9001")
    arg("jimmerDdl.description", "ksp_smoke")
    arg("jimmerDdl.includeComments", "false")
}

tasks.register("verifyGeneratedJimmerDdl") {
    dependsOn("kspKotlin")
    inputs.file(generatedDdlFilePath)
    doLast {
        val sqlFile = file(generatedDdlFilePath)
        require(sqlFile.exists()) {
            "未生成 KSP Jimmer DDL: ${sqlFile.absolutePath}"
        }
        val sql = sqlFile.readText()
        require(sql.contains("CREATE TABLE \"ksp_book\"")) {
            "DDL 未包含 ksp_book 建表语句: $sql"
        }
        require(sql.contains("CREATE UNIQUE INDEX \"uk_ksp_book_title\"")) {
            "DDL 未包含 title 唯一索引: $sql"
        }
        require(sql.contains("\"tags\" JSON NOT NULL")) {
            "DDL 未包含 @Serialized 集合 JSON 字段: $sql"
        }
    }
}
