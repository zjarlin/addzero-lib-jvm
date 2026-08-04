package site.addzero.gradle.tool

import org.apache.tools.ant.util.FileUtils.getRelativePath
import org.gradle.api.initialization.Settings
import java.io.File

private val ignoredProjectDirectoryNames = setOf(
  "build",
  "node_modules",
  "out",
  "target",
)

/**
 * 查找源码树中所有包含 `build.gradle.kts` 的项目目录。
 *
 * 构建产物、隐藏目录和前端依赖目录不会包含需要装配的源码模块，
 * 必须在递归入口直接剪枝，避免配置缓存把自身文件记录为构建输入。
 */
fun findAllProjectDirs(rootDir: File): List<File> =
  rootDir.walkTopDown()
    .onEnter { directory ->
      directory == rootDir || (
        !directory.name.startsWith(".") &&
          directory.name !in ignoredProjectDirectoryNames
        )
    }
    .filter { directory ->
      directory.isDirectory && directory.resolve("build.gradle.kts").isFile
    }
    .toList()

fun getProjectContext(
  rootDir: File, predicate: (File) -> Boolean = { true },
): ProjectContext {
  val findAllProjectDirs = findAllProjectDirs(rootDir)
  val allProjectDirs = findAllProjectDirs.filter { it.absolutePath != rootDir.absolutePath } // 排除根项目本身

  val (buildProj, modules) = allProjectDirs.partition {
    val isBuildLogic = it.name.startsWith("build-logic")
      ||
      it.name.startsWith("buildLogic")
//                ||
//                it.name.startsWith("buildSrc")
    isBuildLogic
  }
  val partition = modules.partition {
    predicate(it)
  }
  val projectContext =
    ProjectContext(buildProj, partition.first.filterNot { it.name.startsWith("buildSrc") }, partition.second)
  return projectContext
}

private fun String.toGradleProjectPath(): String =
  split(File.separatorChar, '/', '\\')
    .filter { it.isNotBlank() }
    .joinToString(separator = ":", prefix = ":")

fun File.isInBlackList(rootDir: File, vararg blackModuleName: String): Boolean {
  // 隐藏目录及其后代不参与模块装配。
  if (this.name.startsWith(".")) return true

  val relativePath = getRelativePath(rootDir, this)
  if (relativePath.split(File.separatorChar, '/').any { it.startsWith(".") }) {
    return true
  }

  if (blackModuleName.isEmpty()) return false

  val moduleName = relativePath.toGradleProjectPath()
  val leafModuleName = this.name

  return blackModuleName.any { black ->
    moduleName == black ||
      leafModuleName == black ||
      moduleName.startsWith(":$black:") ||
      moduleName == ":$black"
  }
}

fun Settings.autoIncludeModules(vararg blackModuleName: String) {
  val rootDir = settings.rootDir
  autoIncludeModules {
    val inBlackList = it.isInBlackList(rootDir, *blackModuleName)
    !inBlackList
  }
}

fun Settings.autoIncludeModules(predicate: (File) -> Boolean = { true }) {
  val rootDir = settings.rootDir
  val projectContext = getProjectContext(rootDir, predicate)

  val includedBuilds = mutableListOf<String>()
  projectContext.buildLogics.forEach {
    settings.includeBuild(it)
    println("🛠️find build logic: ${it.name}")
    includedBuilds += it.name
  }

  val includedModules = mutableListOf<String>()
  projectContext.modules
    .filterNot { it.name == "buildSrc" }
    .forEach {
      val relativePath = getRelativePath(rootDir, it)
      val moduleName = relativePath.toGradleProjectPath()
      settings.include(moduleName)
      println("📦find module: $moduleName")
      includedModules += moduleName
    }

  val skippedModules = mutableListOf<String>()
  projectContext.blackModules.forEach {
    val relativePath = getRelativePath(rootDir, it)
    val moduleName = relativePath.toGradleProjectPath()
    println("⏭️  Skipped module: $moduleName")
    skippedModules += moduleName
  }

  fun sample(list: List<String>): String =
    if (list.isEmpty()) "-" else list.take(5).joinToString(", ") + if (list.size > 5) ", ..." else ""

  println(
    """
================ Modules Buddy Summary ================
🔧 Build logics included: ${includedBuilds.size} (${sample(includedBuilds)})
📦 Modules included: ${includedModules.size} (${sample(includedModules)})
🚫 Modules skipped: ${skippedModules.size} (${sample(skippedModules)})
======================================================
""".trimIndent()
  )
}

// 检查目录是否在黑名单中
//private fun isBlacklisted(projectDir: File, rootDir: File, blackDir: List<String>): Boolean {
//    val relativePath = getRelativePath(rootDir, projectDir)
//    return blackDir.any { blacklisted ->
//        relativePath == blacklisted || relativePath.startsWith("$blacklisted${File.separator}")
//    }
//}
