package site.addzero.gradle.tool

import org.apache.tools.ant.util.FileUtils
import org.apache.tools.ant.util.FileUtils.getRelativePath
import org.gradle.api.initialization.Settings
import java.io.File

// 递归查找所有包含build.gradle.kts的项目目录
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

fun File.isInBlackList(rootDir: File, vararg blackModuleName: String): Boolean {
  // Skip any hidden directories (segments that start with ".")
  if (this.name.startsWith(".")) return true

  val relativePath = FileUtils.getRelativePath(rootDir, this)
  if (relativePath.split(File.separatorChar, '/').any { it.startsWith(".") }) {
    return true
  }

  if (blackModuleName.isEmpty()) return false

  val moduleName = ":${relativePath.replace(File.separator, ":")}"
  val leafModuleName = this.name

  return blackModuleName.any { black ->
    moduleName == black ||
      leafModuleName == black ||
      moduleName.startsWith(":$black:") ||
      moduleName == ":$black"
  }
}

fun getProjectContext(
  rootDir: File, vararg blackModuleName: String,
): ProjectContext {
  return getProjectContext(rootDir) {
    val inBlackList = it.isInBlackList(rootDir, *blackModuleName)
    !inBlackList
  }
}

fun Settings.autoIncludeModules(vararg blackModuleName: String) {
  val rootDir = settings.layout.rootDirectory.asFile
  settings.autoIncludeModules {
    val inBlackList = it.isInBlackList(rootDir, *blackModuleName)
    !inBlackList
  }
}

fun Settings.autoIncludeModules(predicate: (File) -> Boolean = { true }) {
  val rootDir = settings.layout.rootDirectory.asFile
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
      val moduleName = ":${relativePath.replace(File.separator, ":")}"
      settings.include(moduleName)
      println("📦find module: $moduleName")
      includedModules += moduleName
    }

  val skippedModules = mutableListOf<String>()
  projectContext.blackModules.forEach {
    val relativePath = getRelativePath(rootDir, it)
    val moduleName = ":${relativePath.replace(File.separator, ":")}"
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

/**
 * 将路径转换为小驼峰命名
 */
private fun String.toCamelCase(): String {
  val parts = this.split("-", ":")
  return parts.mapIndexed { index, part ->
    if (index == 0) {
      part.replaceFirstChar { it.lowercase() }
    } else {
      part.replaceFirstChar { it.uppercase() }
    }
  }.joinToString("")
}
