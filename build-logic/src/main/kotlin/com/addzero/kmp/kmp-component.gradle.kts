import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp")
    id("kmp-android-library")
    id("com.google.devtools.ksp")
}



//val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
//dependencies {
//    kspCommonMainMetadata(libs.koin.ksp.compiler)
//}

//kotlin {
//    sourceSets {
//        commonMain.dependencies {
//            implementation(project.dependencies.platform(libs.koin.bom))
//            implementation(libs.koin.annotations)
//            implementation(libs.koin.core)
//            implementation(libs.koin.compose)
//            implementation(libs.koin.compose.viewmodel)
//            implementation(libs.koin.compose.viewmodel.navigation)
//        }
//    }
//}

//tasks.withType<KotlinCompilationTask<*>>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn(":backend:model:kspKotlin")
//        dependsOn(":backend:server:kspKotlin")
//        dependsOn(":shared:kspCommonMainKotlinMetadata")
//        dependsOn(":lib:addzero-compose-native-component:kspCommonMainKotlinMetadata")
//        dependsOn(":lib:addzero-compose-klibs-component:kspCommonMainKotlinMetadata")
//        dependsOn(":composeApp:kspCommonMainKotlinMetadata")
//    }
//}



