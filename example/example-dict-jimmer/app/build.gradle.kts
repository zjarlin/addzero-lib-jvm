import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("site.addzero.buildlogic.jvm.jimmer")
    application
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:tool-starter:dict-trans-spring-boot-starter"))
    implementation(libs.findLibrary("org-springframework-spring-context").get())
}

application {
    mainClass.set("site.addzero.example.dictjimmer.DictJimmerSmokeKt")
}

tasks.register<JavaExec>("runDictJimmerSmoke") {
    group = "verification"
    description = "验证 Jimmer 实体属性上的 @Dict 能被字典翻译识别并输出翻译字段。"
    dependsOn("classes")
    val mainSourceSet = project.extensions.getByType(SourceSetContainer::class.java).named("main").get()
    classpath(mainSourceSet.runtimeClasspath)
    mainClass.set("site.addzero.example.dictjimmer.DictJimmerSmokeKt")
    jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
    )
}
