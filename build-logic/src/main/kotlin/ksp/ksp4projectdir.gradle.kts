
import Vars.commonMainKspBuildMetaDataDir
import Vars.commonMainSourceDir
import Vars.jvmMainKspBuildMetaDataDir
import Vars.jvmMainResourceDir
import Vars.jvmMainSourceDir

// 计算各模块目录（使用常量字符串）


val serverProject = project(":backend:server")
val composeProject = project(":composeApp")
val sharedProject = project(":shared")
val serverSourceDir = serverProject.projectDir.resolve(jvmMainSourceDir).absolutePath
val serverResourceDir = serverProject.projectDir.resolve(jvmMainResourceDir) .absolutePath

val serverBuildDir = serverProject.projectDir.resolve(jvmMainKspBuildMetaDataDir).absolutePath


val composeSourceDir = composeProject.projectDir.resolve(commonMainSourceDir).absolutePath
val composeBuildDir = composeProject.projectDir.resolve(commonMainKspBuildMetaDataDir).absolutePath

val sharedSourceDir = sharedProject.projectDir.resolve(commonMainSourceDir).absolutePath
val sharedBuildDir = sharedProject.projectDir.resolve(commonMainKspBuildMetaDataDir).absolutePath


val modelProject = project(":backend:model")
val modelSourceDir = modelProject.projectDir.resolve(jvmMainSourceDir).absolutePath
val modelBuildDir = modelProject.projectDir.resolve(jvmMainKspBuildMetaDataDir).absolutePath


val sharedComposeProject = project(":shared-compose")
val sharedComposeSourceDir = sharedComposeProject.projectDir.resolve(commonMainSourceDir).absolutePath
val sharedComposeBuildDir = sharedComposeProject.projectDir.resolve(commonMainKspBuildMetaDataDir).absolutePath



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












