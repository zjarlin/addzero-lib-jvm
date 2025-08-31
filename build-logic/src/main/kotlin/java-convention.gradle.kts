plugins {
    `java-library`
    // 暂时移除发布相关插件
    // id("publish-convention")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

val jdkversion = libs.versions.jdk.get()
extensions.configure<JavaPluginExtension> {
    val toVersion = JavaVersion.toVersion(jdkversion)
    sourceCompatibility = toVersion
    targetCompatibility = toVersion
    withSourcesJar()
//    withJavadocJar()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkversion.toInt()))
    }
}

tasks.test {
    useJUnitPlatform()
}


tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}
