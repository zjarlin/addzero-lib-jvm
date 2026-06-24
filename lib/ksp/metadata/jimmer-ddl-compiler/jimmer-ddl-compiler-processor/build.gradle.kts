plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

val catalogLibs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.jimmer.ddl.compiler.context")
    mustMap.set(
        mapOf(
            "jimmerDdl.enabled" to "true",
            "jimmerDdl.profiles" to "",
            "jimmerDdl.databaseType" to "postgresql",
            "jimmerDdl.outputFormat" to "flyway",
            "jimmerDdl.outputDir" to "build/generated/jimmer-ddl/main/resources/db/migration",
            "jimmerDdl.version" to "1001",
            "jimmerDdl.description" to "jimmer_auto_ddl_generated",
            "jimmerDdl.includePackages" to "",
            "jimmerDdl.excludePackages" to "",
            "jimmerDdl.includeForeignKeys" to "true",
            "jimmerDdl.includeIndexes" to "true",
            "jimmerDdl.includeComments" to "true",
            "jimmerDdl.includeSequences" to "true",
            "jimmerDdl.includeManyToManyTables" to "true",
        )
    )
}

dependencies {
    implementation(project(":lib:lsi:lsi-core"))
    implementation(project(":lib:lsi:lsi-ksp"))
    implementation(project(":lib:lsi:lsi-apt"))
    implementation(project(":lib:lsi:lsi-jimmer"))
    implementation(project(":lib:tool-jvm:database:ddlgenerator"))
    implementation(project(":lib:tool-jvm:database:ddlgenerator-core"))
    implementation(project(":lib:tool-jvm:database:tool-database-model"))
    implementation(catalogLibs.findLibrary("javax-annotation-javax-annotation-api").get())

    testImplementation(kotlin("test"))
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

tasks.test {
    useJUnitPlatform()
}

description = "Jimmer DDL 编译期生成器，基于 LSI 同时支持 Kotlin KSP 与 Java APT 实体"
