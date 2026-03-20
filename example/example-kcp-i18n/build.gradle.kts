import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    application
}

dependencies {
    implementation(project(":lib:kcp:kcp-i18n-runtime"))
}

val i18nPluginJarPath = provider {
    rootProject
        .file("lib/kcp/kcp-i18n/build/libs/kcp-i18n-${project.version}.jar")
        .absolutePath
}

tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn(":lib:kcp:kcp-i18n:jar")
    compilerOptions.freeCompilerArgs.addAll(
        i18nPluginJarPath.map { pluginJarPath ->
            listOf(
                "-Xplugin=$pluginJarPath",
                "-P",
                "plugin:site.addzero.kcp.i18n:targetLocale=en",
                "-P",
                "plugin:site.addzero.kcp.i18n:resourceBasePath=i18n",
            )
        },
    )
}

application {
    mainClass.set("site.addzero.example.MainKt")
}
