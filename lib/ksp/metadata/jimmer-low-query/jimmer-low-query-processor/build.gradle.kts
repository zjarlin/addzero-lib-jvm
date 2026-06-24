plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

version = "2026.06.26"

val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.jimmer.lowquery.processor.context")
    mustMap.set(
        mapOf(
            "jimmerLowQuery.generatedPackage" to "",
        )
    )
}

dependencies {
    implementation(project(":lib:ksp:metadata:jimmer-low-query:jimmer-low-query-annotations"))
    testImplementation(kotlin("test"))
}
