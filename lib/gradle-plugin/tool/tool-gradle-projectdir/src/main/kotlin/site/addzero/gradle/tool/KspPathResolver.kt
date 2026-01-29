package site.addzero.gradle.tool

import org.gradle.api.Project
import java.io.File

object KspPathResolver {

    fun getModuleProject(project: Project, modulePath: String): Project {
        return project.rootProject.project(modulePath)
    }

    fun getJvmSourceDir(moduleProject: Project): File {
        return moduleProject.projectDir.resolve("src/main/kotlin")
    }

    fun getJvmResourceDir(moduleProject: Project): File {
        return moduleProject.projectDir.resolve("src/main/resources")
    }

    fun getKmpCommonSourceDir(moduleProject: Project): File {
        return moduleProject.projectDir.resolve("src/commonMain/kotlin")
    }

    // KSP build directories are typically relative to the module's build directory
    // For JVM modules, it's usually build/generated/ksp/main/kotlin
    fun getJvmKspBuildDir(moduleProject: Project): File {
        return moduleProject.buildDir.resolve("generated/ksp/main/kotlin")
    }

    // For KMP common metadata, it's usually build/generated/ksp/metadata/commonMain/kotlin
    fun getKmpCommonKspBuildDir(moduleProject: Project): File {
        return moduleProject.buildDir.resolve("generated/ksp/metadata/commonMain/kotlin")
    }
}
