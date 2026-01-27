package site.addzero.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * 自动为包含顶层函数的 Kotlin 文件添加 @file:JvmName 注解
 * 约定：文件名（去除.kt后缀）作为 JvmName
 */
abstract class AutoJvmNameTask : DefaultTask() {

    @get:InputFiles
    val sourceFiles: FileTree = project.fileTree("src/main/kotlin") {
        include("**/*.kt")
    }

    @TaskAction
    fun addJvmNameAnnotations() {
        var modifiedCount = 0
        var skippedCount = 0

        sourceFiles.forEach { file ->
            if (shouldProcessFile(file)) {
                val content = file.readText()
                
                // 检查是否已有 @file:JvmName
                if (!content.contains("@file:JvmName")) {
                    // 检查是否有顶层函数或顶层属性
                    if (hasTopLevelDeclarations(content)) {
                        val fileName = file.nameWithoutExtension
                        val newContent = addJvmNameAnnotation(content, fileName)
                        
                        if (newContent != content) {
                            file.writeText(newContent)
                            modifiedCount++
                            logger.lifecycle("Added @file:JvmName(\"$fileName\") to ${file.relativeTo(project.projectDir)}")
                        }
                    } else {
                        skippedCount++
                    }
                } else {
                    skippedCount++
                }
            }
        }

        logger.lifecycle("AutoJvmName: Modified $modifiedCount files, skipped $skippedCount files")
    }

    private fun shouldProcessFile(file: File): Boolean {
        // 排除 build 目录和测试文件
        val path = file.absolutePath
        return !path.contains("/build/") && 
               !path.contains("/test/") &&
               !path.contains("/androidTest/")
    }

    private fun hasTopLevelDeclarations(content: String): Boolean {
        // 简单检测：是否有顶层函数声明
        // 匹配 fun 关键字，不在 class/interface/object 内部
        val lines = content.lines()
        var inClassOrInterface = 0
        var foundTopLevel = false

        for (line in lines) {
            val trimmed = line.trim()
            
            // 跳过注释和空行
            if (trimmed.startsWith("//") || trimmed.isEmpty()) continue
            
            // 跟踪嵌套层级
            if (trimmed.matches(Regex("^(class|interface|object|enum class|sealed class|data class|value class)\\s+.*\\{"))) {
                inClassOrInterface++
            }
            
            inClassOrInterface += trimmed.count { it == '{' }
            inClassOrInterface -= trimmed.count { it == '}' }
            
            // 检查顶层声明
            if (inClassOrInterface == 0) {
                if (trimmed.startsWith("fun ") || 
                    trimmed.startsWith("inline fun ") ||
                    trimmed.startsWith("suspend fun ") ||
                    trimmed.matches(Regex("^(val|var|const val)\\s+.*"))) {
                    foundTopLevel = true
                    break
                }
            }
        }

        return foundTopLevel
    }

    private fun addJvmNameAnnotation(content: String, fileName: String): String {
        val annotation = "@file:JvmName(\"$fileName\")"
        
        // 查找 package 声明的位置
        val packageRegex = Regex("^package\\s+[\\w.]+\\s*$", RegexOption.MULTILINE)
        val packageMatch = packageRegex.find(content)
        
        return if (packageMatch != null) {
            // 在 package 声明后插入
            val insertPosition = packageMatch.range.last + 1
            val beforePackage = content.substring(0, insertPosition)
            val afterPackage = content.substring(insertPosition)
            
            "$beforePackage\n\n$annotation$afterPackage"
        } else {
            // 在文件开头插入（在 @file 注解和 import 之前）
            val firstImportIndex = content.indexOf("\nimport ")
            if (firstImportIndex > 0) {
                val beforeImports = content.substring(0, firstImportIndex)
                val afterImports = content.substring(firstImportIndex)
                "$annotation\n$beforeImports$afterImports"
            } else {
                "$annotation\n\n$content"
            }
        }
    }
}

// 注册任务
tasks.register<AutoJvmNameTask>("autoJvmName") {
    group = "kotlin"
    description = "Automatically add @file:JvmName annotations to Kotlin files with top-level declarations"
}

// 可选：在编译前自动运行
tasks.named("compileKotlin") {
    // dependsOn("autoJvmName")  // 取消注释以在编译前自动运行
}
