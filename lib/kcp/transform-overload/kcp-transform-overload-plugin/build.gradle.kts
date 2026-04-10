import org.gradle.jvm.tasks.Jar

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

group = "site.addzero"

dependencies {
    implementation(libs.findLibrary("kcp-transform-overload-annotations").get())
    compileOnly(libs.findLibrary("org-jetbrains-kotlin-kotlin-compiler-embeddable").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-compiler-embeddable").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

val pluginJar = tasks.named<Jar>("jar").flatMap { task -> task.archiveFile }

tasks.test {
    dependsOn(pluginJar)
    systemProperty("transformOverload.pluginJar", pluginJar.get().asFile.absolutePath)
}
