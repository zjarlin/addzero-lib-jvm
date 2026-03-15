plugins {
    `kotlin-dsl`
}
repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}
dependencies {
    val libs = versionCatalogs.named("libs")
    gradleApi()
//    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    // 关键：添加 Guice 依赖，解决 Sisu 模块的父类解析问题
    implementation(libs.findLibrary("com-google-inject-guice").get()) // 稳定兼容版本
    implementation(libs.findLibrary("site-addzero-tool-yml").get()) // 稳定兼容版本
    implementation(libs.findLibrary("site-addzero-tool-gradle-projectdir").get())
    implementation(libs.findLibrary("io-insert-koin-koin-compiler-gradle-plugin").get())
    implementation(libs.findLibrary("io-ktor-plugin-plugin").get())
    ////////////jvm/////////////////////
//    implementation(libs.site.addzero.gradle.tool)
    implementation(libs.findLibrary("com-diffplug-spotless-com-diffplug-spotless-gradle-plugin").get())

    implementation(libs.findLibrary("org-jetbrains-dokka-dokka-gradle-plugin").get())

    implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-gradle-plugin").get())
    implementation(libs.findLibrary("org-graalvm-buildtools-native-org-graalvm-buildtools-native-gradle-plugin").get())

    ////////////////spring//////////////
    implementation(libs.findLibrary("io-spring-gradle-dependency-management-plugin").get())
    implementation(libs.findLibrary("org-springframework-boot-org-springframework-boot-gradle-plugin-v2").get())
    implementation(libs.findLibrary("org-jetbrains-kotlin-plugin-spring-org-jetbrains-kotlin-plugin-spring-gradle-plugin").get())

    /////////////////intellij///////////////
    implementation(libs.findLibrary("org-jetbrains-intellij-platform-org-jetbrains-intellij-platform-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-changelog-org-jetbrains-changelog-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-intellij-platform-migration-org-jetbrains-intellij-platform-migration-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-intellij-platform-settings-org-jetbrains-intellij-platform-settings-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-intellij-platform-base-org-jetbrains-intellij-platform-base-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-intellij-platform-module-org-jetbrains-intellij-platform-module-gradle-plugin").get())
    /////////////////kmp///////////////
    implementation(libs.findLibrary("org-jetbrains-compose-compose-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-kotlin-plugin-compose-org-jetbrains-kotlin-plugin-compose-gradle-plugin").get())
    implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-serialization").get())

    implementation(libs.findLibrary("com-codingfeline-buildkonfig-buildkonfig-gradle-plugin").get())
    implementation(libs.findLibrary("com-codingfeline-buildkonfig-buildkonfig-compiler").get())

    implementation(libs.findLibrary("de-jensklingenberg-ktorfit-de-jensklingenberg-ktorfit-gradle-plugin").get())
    implementation(libs.findLibrary("com-android-tools-build-gradle").get())
    ///////////////ksp///////////
    implementation(libs.findLibrary("com-google-devtools-ksp-com-google-devtools-ksp-gradle-plugin").get())
}
