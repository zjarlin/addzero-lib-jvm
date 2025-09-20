package util

import org.gradle.api.Project
import site.addzero.gradle.tool.generateProjectDirConfigMap
//import site.addzero.gradle.tool.getProjectDirConfigMap
//import site.addzero.gradle.tool.getProjectDirConfigMap


//fun Project.getProjectDirConfigMap(): MutableMap<String, String> {
//    val projectDirConfigMapResult = generateProjectDirConfigMap()
//    val mutableMapOf = mutableMapOf<String, String>()
//
//    projectDirConfigMapResult.configs.forEach { (_, projectConfig) ->
//        mutableMapOf["${projectConfig.moduleName}SourceDir"] = projectConfig.sourceDir
//        mutableMapOf["${projectConfig.moduleName}BuildDir"] = projectConfig.buildDir
//
//        projectConfig.resourceDir?.let {
//            mutableMapOf["${projectConfig.moduleName}ResourceDir"] = it
//        }
//    }
//    return mutableMapOf
//
//}

