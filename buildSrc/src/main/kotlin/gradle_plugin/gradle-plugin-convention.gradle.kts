//package gradle_plugin
//
//plugins {
////  id("org.gradle.kotlin.kotlin-dsl")
////    id("org.gradle.java-gradle-plugin")
//       `kotlin-dsl`
//    `java-gradle-plugin`
//
//}
//
//
//dependencies {
//    implementation(gradleApi())
//}
//
//val pluginName = project.name
//gradlePlugin {
//    plugins {
//        create(pluginName) {
//            id = pluginName
//            implementationClass = "site.addzero.gradle.plugin.$pluginName.${pluginName.replaceFirstChar { it.uppercase() }}Plugin"
//            displayName = "${pluginName.replaceFirstChar { it.uppercase() }} Plugin"
//        }
//    }
//}
