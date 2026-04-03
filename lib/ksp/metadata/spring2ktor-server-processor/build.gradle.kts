plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.springktor.processor.context")
    mustMap.set(
        mapOf(
            "springKtor.generatedPackage" to "",
        )
    )
}

dependencies {
    testImplementation(libs.findLibrary("org-springframework-spring-context").get())
    testImplementation(libs.findLibrary("org-springframework-spring-web").get())
}
