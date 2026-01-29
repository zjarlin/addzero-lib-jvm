package site.addzero.gradle.plugin

// KSP task dependency tweaks (kept disabled by default).
// Uncomment if you need to force task ordering.
//
//import com.google.devtools.ksp.gradle.KspAATask
//import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//tasks.withType<KotlinCompile>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
//
//tasks.withType<Kotlin2JsCompile>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
//
//tasks.withType<Jar>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
//
//tasks.withType<KspAATask>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
//
//tasks.configureEach {
//    if (name.contains("sourcesJar")) {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
