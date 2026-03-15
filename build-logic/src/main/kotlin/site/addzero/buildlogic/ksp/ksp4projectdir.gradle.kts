package site.addzero.buildlogic.ksp

import site.addzero.gradle.tool.ProjectDirScanner
plugins {
    id("com.google.devtools.ksp")
}

val rootDir1 = project.rootDir
ksp {
     val projectPaths = ProjectDirScanner.scanAndMapProjectPaths(project.rootProject.projectDir)
    projectPaths.forEach { (key, value) ->
        arg(key, value)
    }
}
