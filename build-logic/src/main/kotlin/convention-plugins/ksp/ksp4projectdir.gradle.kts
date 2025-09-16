import util.ProjectDirConfigMapResult
import util.generateProjectDirConfigMap

plugins {
    id("com.google.devtools.ksp")
}

val projectDirConfigMapResult: ProjectDirConfigMapResult = generateProjectDirConfigMap()

ksp {
    projectDirConfigMapResult.configs.forEach { (_, projectConfig) ->
        arg("${projectConfig.moduleName}SourceDir", projectConfig.sourceDir)
        arg("${projectConfig.moduleName}BuildDir", projectConfig.buildDir)
        projectConfig.resourceDir?.let {
            arg("${projectConfig.moduleName}ResourceDir", it)
        }
    }
}