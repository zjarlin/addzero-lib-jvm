import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")
val sourceSets = the<SourceSetContainer>()

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())

    testImplementation(project(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base"))
    testImplementation(project(":lib:ksp:route:route-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:gen-reified:gen-reified-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:ioc:ioc-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:jimmer-entity-external-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:multireceiver-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:singleton-adapter-gradle-plugin"))
    testImplementation(project(":lib:ksp:metadata:spring2ktor-server-gradle-plugin"))
}

tasks.test {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty(
        "publishedKsp.testRuntimeClasspath",
        sourceSets.named("test").get().runtimeClasspath.asPath,
    )
    systemProperty("publishedKsp.repoRoot", rootDir.absolutePath)
}
