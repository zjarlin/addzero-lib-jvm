
import BuildSettings.KSP_BUILD_DIR_KMP
import BuildSettings.SOURCE_DIR_KMP
import BuildSettings.KSP_BUILD_DIR_JVM
import BuildSettings.RESOURCE_DIR_JVM
import BuildSettings.SOURCE_DIR_JVM

// 计算各模块目录（使用常量字符串）


val serverProject = project(":backend:server")
val composeProject = project(":composeApp")
val sharedProject = project(":shared")
val serverSourceDir = serverProject.projectDir.resolve(SOURCE_DIR_JVM).absolutePath
val serverResourceDir = serverProject.projectDir.resolve(RESOURCE_DIR_JVM) .absolutePath

val serverBuildDir = serverProject.projectDir.resolve(KSP_BUILD_DIR_JVM).absolutePath


val composeSourceDir = composeProject.projectDir.resolve(SOURCE_DIR_KMP).absolutePath
val composeBuildDir = composeProject.projectDir.resolve(KSP_BUILD_DIR_KMP).absolutePath

val sharedSourceDir = sharedProject.projectDir.resolve(SOURCE_DIR_KMP).absolutePath
val sharedBuildDir = sharedProject.projectDir.resolve(KSP_BUILD_DIR_KMP).absolutePath


val modelProject = project(":backend:model")
val modelSourceDir = modelProject.projectDir.resolve(SOURCE_DIR_JVM).absolutePath
val modelBuildDir = modelProject.projectDir.resolve(KSP_BUILD_DIR_JVM).absolutePath


val sharedComposeProject = project(":shared-compose")
val sharedComposeSourceDir = sharedComposeProject.projectDir.resolve(SOURCE_DIR_KMP).absolutePath
val sharedComposeBuildDir = sharedComposeProject.projectDir.resolve(KSP_BUILD_DIR_KMP).absolutePath



plugins {
    id("com.google.devtools.ksp")
}

ksp {
    arg("serverSourceDir", serverSourceDir)
    arg("serverResourceDir", serverResourceDir)
    arg("serverBuildDir", serverBuildDir)
    arg("composeSourceDir", composeSourceDir)
    arg("composeBuildDir", composeBuildDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedBuildDir", sharedBuildDir)
    arg("modelSourceDir", modelSourceDir)
    arg("modelBuildDir", modelBuildDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("sharedComposeBuildDir", sharedComposeBuildDir)

}












