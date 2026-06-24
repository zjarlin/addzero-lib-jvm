plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}

val catalogLibs = versionCatalogs.named("libs")
val generatedDdlDir = layout.buildDirectory.dir("generated/apt/main/resources/db/migration/jimmer-ddl")
val generatedDdlFilePath = generatedDdlDir.get().file("V9002__apt_smoke.sql").asFile.absolutePath

dependencies {
    implementation(catalogLibs.findLibrary("org-babyfish-jimmer-jimmer-core").get())
    annotationProcessor(project(":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-processor"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-AjimmerDdl.databaseType=h2",
            "-AjimmerDdl.outputFormat=flyway",
            "-AjimmerDdl.outputDir=${generatedDdlDir.get().asFile.absolutePath}",
            "-AjimmerDdl.version=9002",
            "-AjimmerDdl.description=apt_smoke",
            "-AjimmerDdl.includeComments=false",
        )
    )
}

tasks.register("verifyGeneratedJimmerDdl") {
    dependsOn("compileJava")
    inputs.file(generatedDdlFilePath)
    doLast {
        val sqlFile = file(generatedDdlFilePath)
        require(sqlFile.exists()) {
            "未生成 APT Jimmer DDL: ${sqlFile.absolutePath}"
        }
        val sql = sqlFile.readText()
        require(sql.contains("CREATE TABLE \"apt_author\"")) {
            "DDL 未包含 apt_author 建表语句: $sql"
        }
        require(sql.contains("CREATE UNIQUE INDEX \"uk_apt_author_name\"")) {
            "DDL 未包含 name 唯一索引: $sql"
        }
        require(sql.contains("\"aliases\" JSON")) {
            "DDL 未包含 @Serialized 集合 JSON 字段: $sql"
        }
    }
}
