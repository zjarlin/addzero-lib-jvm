import Vars.jvmMainSourceDir
import Vars.jvmMainKspBuildMetaDataDir
import Vars.commonMainSourceDir
import Vars.commonMainKspBuildMetaDataDir
import Vars.jvmMainResourceDir

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

}












