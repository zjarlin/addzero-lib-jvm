package site.addzero.gradle.plugin

import site.addzero.gradle.tool.ProjectDirScanner

plugins {
    id("com.google.devtools.ksp")
}

ksp {
    val projectPaths = ProjectDirScanner.scanAndMapProjectPaths(project.rootProject.projectDir)
    projectPaths.forEach { (key, value) ->
        arg(key, value)
    }
}

