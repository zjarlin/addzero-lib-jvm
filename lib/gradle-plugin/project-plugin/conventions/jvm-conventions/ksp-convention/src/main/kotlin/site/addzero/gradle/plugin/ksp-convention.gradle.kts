package site.addzero.gradle.plugin

val extension = extensions.create("kspConvention", site.addzero.gradle.KspConventionExtension::class.java)

afterEvaluate {
    val version = extension.kspVersion.get()
    dependencies {
        add("implementation", "com.google.devtools.ksp:symbol-processing-api:${version}")
    }
}

