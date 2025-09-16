//plugins {
//    `kotlin-dsl`
//    `java-gradle-plugin`
//}
//
//repositories {
//    mavenCentral()
//    gradlePluginPortal()
//}
//dependencies {
//    implementation(gradleApi())
//    implementation(projects.buildLogic)
//    implementation(projects.ext.properties)
//}
//
//gradlePlugin {
//    plugins {
//        create("addzero-auto-modules") {
//            id = "addzero-auto-modules"
//            implementationClass = "site.addzero.gradle.plugin.automodules.AutoModulesPlugin"
//            displayName = "AddZero Auto Modules Plugin"
//            description = "Automatically discovers and includes Gradle modules in the project"
//        }
//    }
//}
