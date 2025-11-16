import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven


fun RepositoryHandler.applyGoogleRepository() {
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
}


fun RepositoryHandler.applyCommonRepositories() {
    applyGoogleRepository()
    mavenCentral()
}

fun RepositoryHandler.applyPluginRepositories() {
    applyCommonRepositories()
    gradlePluginPortal()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
