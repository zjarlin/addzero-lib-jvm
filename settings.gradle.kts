import org.apache.tools.ant.util.FileUtils.getRelativePath

rootProject.name = "addzero"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//includeBuild("build-logic")
//includeBuild("lib/gradle-plugin/addzero-gradle-ksp-buddy")
//includeBuild("lib/gradle-plugin/addzero-gradle-auto-modules-plugin")
includeBuild("build-logic")
//includeBuild("lib/gradle-plugin/addzero-gradle-tool")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositories {
//        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

fun findAllProjectDirs(rootDir: File): List<File> {
    val result = mutableListOf<File>()
    if (File(rootDir, "build.gradle.kts").exists()) {
        result.add(rootDir)
    }
    rootDir.listFiles { file: File ->
        file.isDirectory
    }?.forEach { subDir ->
        result.addAll(findAllProjectDirs(subDir))
    }
    return result
}

val rootDir = settings.layout.rootDirectory.asFile
val findAllProjectDirs = findAllProjectDirs(rootDir)
val allProjectDirs = findAllProjectDirs
    .filter { it.absolutePath != rootDir.absolutePath } // 排除根项目本身
//    .filter {
//        val bool = it.name.contains("buildSrc")
//        if (bool) {
//            println("检测到构建逻辑项目：${it.name}")
//        }
//        bool
//    }
allProjectDirs.forEach {
    val name = it.name
    println("$name")
    println("名字$name")
    val absolutePath = it.absolutePath
    println("绝对路径$absolutePath")
    val relativePath = getRelativePath(rootDir, it)
    println("相对路径$relativePath")
    val moduleName = ":${relativePath.replace(File.separator, ":")}"
    println("模块名$moduleName")
}
// 包含普通项目
val (buildProj, modules) = allProjectDirs.partition {
    val isBuildLogic = it.name.startsWith("build-logic") || it.name.startsWith("buildLogic")
    isBuildLogic
}
buildProj.forEach {
    settings.includeBuild(it)
    // 打印调试信息
    println("Auto included build: ${it.name}")
}
modules.forEach {
    val relativePath = getRelativePath(rootDir, it)
    val moduleName = ":${relativePath.replace(File.separator, ":")}"
    settings.include(moduleName)
    println("Auto included module: $moduleName")
}
