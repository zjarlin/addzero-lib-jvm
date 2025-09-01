
rootProject.name = "addzero"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//includeBuild("build-logic")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral {
//            credentials {
//                username = localProps.getProperty("sonaTokenUser")
//                password = localProps.getProperty("sonaToken")
//            }
        }
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
        mavenCentral {

        }
    }

}



// 扩展函数：智能自动扫描gradle模块

/**
 * 自动扫描并包含所有gradle模块
 * @param rootModules 根模块列表，这些模块会被扫描子模块
 * @param excludeModules 排除的模块列表（支持路径匹配）
 */
fun autoIncludeModules(
    rootModules: List<String> = listOf("."),
    excludeModules: List<String> = emptyList()
) {
    val projectDir = rootProject.projectDir
    val foundModules = mutableSetOf<String>()

    rootModules.forEach { rootModule ->
        val scanDir = if (rootModule == ".") projectDir else File(projectDir, rootModule)
        if (scanDir.exists() && scanDir.isDirectory) {
            scanForGradleModules(scanDir, rootModule, foundModules)
        }
    }

    // 过滤排除的模块
    val filteredModules = foundModules.filter { modulePath ->
        !excludeModules.any { exclude ->
            when {
                exclude.contains("*") -> {
                    // 支持通配符匹配
                    val pattern = exclude.replace("*", ".*")
                    modulePath.matches(Regex(pattern))
                }
                exclude.startsWith(":") -> modulePath == exclude.substring(1)
                else -> modulePath.contains(exclude)
            }
        }
    }

    // 包含所有找到的模块
    filteredModules.forEach { modulePath ->
        if (modulePath != ".") {
            include(":$modulePath")
            findProject(":$modulePath")
            println("✓ 自动包含模块: :$modulePath")
        }
    }

    println("\n🎯 模块扫描完成，共找到 ${filteredModules.size} 个模块")
    if (excludeModules.isNotEmpty()) {
        println("📝 排除的模块模式: ${excludeModules.joinToString(", ")}")
    }
}

/**
 * 递归扫描目录中的gradle模块
 */
fun scanForGradleModules(dir: File, relativePath: String, foundModules: MutableSet<String>) {
    val buildFiles = arrayOf("build.gradle.kts", "build.gradle")

    // 检查当前目录是否包含构建文件
    val hasBuildFile = buildFiles.any { File(dir, it).exists() }

    if (hasBuildFile) {
        val modulePath = if (relativePath == ".") "." else relativePath.replace("/", ":")
        foundModules.add(modulePath)
    }

    // 递归扫描子目录（跳过常见的非模块目录）
    dir.listFiles()?.forEach { subDir ->
        if (subDir.isDirectory && !isExcludedDir(subDir.name)) {
            val subPath = if (relativePath == ".") subDir.name else "$relativePath/${subDir.name}"
            scanForGradleModules(subDir, subPath, foundModules)
        }
    }
}

/**
 * 判断是否为应排除的目录
 */
fun isExcludedDir(dirName: String): Boolean {
    val excludedDirs = setOf(
        "build", "gradle", ".gradle", ".git", ".idea",
        "node_modules", "target", "out", "bin", ".settings",
        "src", "test", "main", "kotlin", "java", "resources"
    )
    return excludedDirs.contains(dirName) || dirName.startsWith(".")
}


// ================== 智能模块扫描 ==================

// 自动扫描所有gradle模块，只需指定根模块和排除的模块
autoIncludeModules(
    rootModules = listOf(
        "backend",
        "composeApp",
        "shared",
        "shared-compose",
        "lib"
    ),
    excludeModules = listOf(
        // 可以在这里添加需要排除的模块
        // 例如: "*test*", "lib:some-disabled-module"
    )
)
