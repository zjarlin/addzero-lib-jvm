//package site.addzero.gradle.plugin.automodules
//
//import org.gradle.api.Plugin
//import org.gradle.api.initialization.Settings
//import site.addzero.gradle.tool.BLACKLIST_DIRS
//import site.addzero.gradle.tool.isExcludedDir
//
///**
// * 自动模块发现插件
// * 该插件在Gradle设置阶段自动发现并包含项目中的所有Gradle模块
// */
//class AutoModulesPlugin : Plugin<Settings> {
//    override fun apply(settings: Settings) {
//        // 在设置阶段执行自动模块发现
//        settings.gradle.settingsEvaluated {
//            autoIncludeModules(settings)
//        }
//    }
//
//    /**
//     * 自动扫描并包含所有gradle模块
//     * @param settings Gradle设置对象
//     */
//    private fun autoIncludeModules(settings: Settings) {
//        val rootDir = settings.rootDir
//        val foundModules = mutableSetOf<String>()
//
//        // 定义根模块列表
//        val rootModules = listOf(
//            "backend",
//            "composeApp",
//            "shared",
//            "shared-compose",
//            "lib"
//        )
//
//        // 定义排除的模块列表
//        val excludeModules = listOf(
//            "addzero-gradle-ksp-buddy"
//        )
//
//        rootModules.forEach { rootModule ->
//            val scanDir = if (rootModule == ".") rootDir else java.io.File(rootDir, rootModule)
//            if (scanDir.exists() && scanDir.isDirectory) {
//                scanForGradleModules(scanDir, rootModule, foundModules)
//            }
//        }
//
//        // 过滤排除的模块
//        val filteredModules = foundModules.filter { modulePath ->
//            !excludeModules.any { exclude ->
//                when {
//                    exclude.contains("*") -> {
//                        // 支持通配符匹配
//                        val pattern = exclude.replace("*", ".*")
//                        modulePath.matches(Regex(pattern))
//                    }
//                    exclude.startsWith(":") -> modulePath == exclude.substring(1)
//                    else -> modulePath.contains(exclude)
//                }
//            }
//        }
//
//        // 包含所有找到的模块
//        filteredModules.forEach { modulePath ->
//            if (modulePath != ".") {
//                settings.include(":$modulePath")
//                println("✓ 自动包含模块: :$modulePath")
//            }
//        }
//
//        println("\n🎯 模块扫描完成，共找到 ${filteredModules.size} 个模块")
//        if (excludeModules.isNotEmpty()) {
//            println("📝 排除的模块模式: ${excludeModules.joinToString(", ")}")
//        }
//    }
//
//    /**
//     * 递归扫描目录中的gradle模块
//     */
//    private fun scanForGradleModules(dir: java.io.File, relativePath: String, foundModules: MutableSet<String>) {
//        val buildFiles = arrayOf("build.gradle.kts", "build.gradle")
//
//        // 检查当前目录是否包含构建文件
//        val hasBuildFile = buildFiles.any { java.io.File(dir, it).exists() }
//
//        if (hasBuildFile) {
//            val modulePath = if (relativePath == ".") "." else relativePath.replace("/", ":")
//            foundModules.add(modulePath)
//        }
//
//        // 递归扫描子目录（跳过常见的非模块目录）
//        dir.listFiles()?.forEach { subDir ->
//            if (subDir.isDirectory && !isExcludedDir(subDir.name) && !isBlacklisted(subDir, dir)) {
//                val subPath = if (relativePath == ".") subDir.name else "$relativePath/${subDir.name}"
//                scanForGradleModules(subDir, subPath, foundModules)
//            }
//        }
//    }
//
//    /**
//     * 检查目录是否在黑名单中
//     */
//    private fun isBlacklisted(projectDir: java.io.File, rootDir: java.io.File): Boolean {
//        val relativePath = getRelativePath(rootDir, projectDir)
//        return BLACKLIST_DIRS.any { blacklisted ->
//            relativePath == blacklisted || relativePath.startsWith("$blacklisted${java.io.File.separator}")
//        }
//    }
//
//    /**
//     * 手动计算相对路径（兼容低版本）
//     */
//    private fun getRelativePath(rootDir: java.io.File, projectDir: java.io.File): String {
//        val rootPath = rootDir.absolutePath
//        val projectPath = projectDir.absolutePath
//        return if (projectPath.startsWith(rootPath)) {
//            projectPath.substring(rootPath.length).trimStart(java.io.File.separatorChar)
//        } else {
//            projectPath
//        }
//    }
//}
