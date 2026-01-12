package site.addzero.util.lsi_impl.impl.intellij.module

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * IntelliJ Module 工具类
 * 
 * 提供模块相关的便捷操作，包括模块查找、信息获取、依赖关系等
 * 
 * 注意：这是一个工具类库，部分方法可能暂未使用，但提供了完整的 API
 */

@Suppress("unused", "MemberVisibilityCanBePrivate")

// ==================== 模块获取 ====================

/**
 * 获取项目中的所有模块
 * 
 * @return 模块数组
 */
fun Project.getAllModules(): Array<Module> {
    return ModuleManager.getInstance(this).modules
}

/**
 * 根据模块名称获取模块
 * 
 * @param moduleName 模块名称
 * @return 找到的模块，如果不存在则返回 null
 */
fun Project.findModuleByName(moduleName: String): Module? {
    return ModuleManager.getInstance(this).findModuleByName(moduleName)
}

/**
 * 获取文件所属的模块
 * 
 * @param file 虚拟文件
 * @return 所属模块，如果不在任何模块中则返回 null
 */
fun Project.findModuleForFile(file: VirtualFile): Module? {
    return com.intellij.openapi.module.ModuleUtilCore.findModuleForFile(file, this)
}

/**
 * 获取 PsiFile 所属的模块
 * 
 * @return 所属模块，如果不在任何模块中则返回 null
 */
fun PsiFile.findModule(): Module? {
    return com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement(this)
}

// ==================== 模块信息 ====================

/**
 * 获取模块的根目录
 * 
 * @return 模块根目录路径，如果不存在则返回 null
 */
val Module.rootPath: String?
    get() = ModuleRootManager.getInstance(this).contentRoots.firstOrNull()?.path

/**
 * 获取模块的所有内容根目录
 * 
 * @return 内容根目录数组
 */
val Module.contentRoots: Array<VirtualFile>
    get() = ModuleRootManager.getInstance(this).contentRoots

/**
 * 获取模块的所有源码根目录
 * 
 * @return 源码根目录数组
 */
val Module.sourceRoots: Array<VirtualFile>
    get() = ModuleRootManager.getInstance(this).sourceRoots

/**
 * 获取模块的所有测试源码根目录
 * 
 * 注意：此实现返回所有源码根目录，需要调用者自行判断是否为测试目录
 * 
 * @return 测试源码根目录数组
 */
val Module.testSourceRoots: Array<VirtualFile>
    get() {
        val fileIndex = com.intellij.openapi.roots.ProjectFileIndex.getInstance(this.project)
        return sourceRoots.filter { root ->
            fileIndex.isInTestSourceContent(root)
        }.toTypedArray()
    }

/**
 * 获取模块的排除目录
 * 
 * @return 排除目录数组
 */
val Module.excludeRoots: Array<VirtualFile>
    get() = ModuleRootManager.getInstance(this).excludeRoots

// ==================== 模块判断 ====================

/**
 * 判断文件是否在模块的源码目录中
 * 
 * @param file 虚拟文件
 * @return true 表示在源码目录中，false 表示不在
 */
fun Module.isInSourceContent(file: VirtualFile): Boolean {
    val fileIndex = com.intellij.openapi.roots.ProjectFileIndex.getInstance(this.project)
    return fileIndex.isInSourceContent(file) && fileIndex.getModuleForFile(file) == this
}

/**
 * 判断文件是否在模块的测试源码目录中
 * 
 * @param file 虚拟文件
 * @return true 表示在测试源码目录中，false 表示不在
 */
fun Module.isInTestSourceContent(file: VirtualFile): Boolean {
    val fileIndex = com.intellij.openapi.roots.ProjectFileIndex.getInstance(this.project)
    return fileIndex.isInTestSourceContent(file) && fileIndex.getModuleForFile(file) == this
}

/**
 * 判断文件是否在模块的排除目录中
 * 
 * @param file 虚拟文件
 * @return true 表示被排除，false 表示未被排除
 */
fun Module.isExcluded(file: VirtualFile): Boolean {
    val fileIndex = com.intellij.openapi.roots.ProjectFileIndex.getInstance(this.project)
    return fileIndex.isExcluded(file)
}

/**
 * 判断模块是否有 Java/Kotlin 源码
 * 
 * @return true 表示有源码，false 表示无源码（可能是资源模块）
 */
val Module.hasSourceRoots: Boolean
    get() = sourceRoots.isNotEmpty()

// ==================== 模块依赖 ====================

/**
 * 获取模块依赖的所有模块
 * 
 * @param includeTests 是否包含测试依赖，默认为 false
 * @return 依赖的模块数组
 */
fun Module.getDependencies(includeTests: Boolean = false): Array<Module> {
    val orderEntries = ModuleRootManager.getInstance(this).orderEntries
    return orderEntries
        .asSequence()
        .filterIsInstance<com.intellij.openapi.roots.ModuleOrderEntry>()
        .filter { includeTests || it.scope != com.intellij.openapi.roots.DependencyScope.TEST }
        .mapNotNull { it.module }
        .toList()
        .toTypedArray()
}

/**
 * 获取依赖当前模块的所有模块
 * 
 * @return 依赖当前模块的模块列表
 */
fun Module.getDependentModules(): List<Module> {
    val project = this.project
    return project.getAllModules().filter { module ->
        module.getDependencies(includeTests = true).contains(this)
    }
}

/**
 * 判断当前模块是否依赖指定模块
 * 
 * @param targetModule 目标模块
 * @param includeTests 是否包含测试依赖
 * @return true 表示依赖，false 表示不依赖
 */
fun Module.dependsOn(targetModule: Module, includeTests: Boolean = false): Boolean {
    return getDependencies(includeTests).contains(targetModule)
}

/**
 * 获取模块的所有传递依赖（递归获取）
 * 
 * @param includeTests 是否包含测试依赖
 * @return 所有传递依赖的模块集合
 */
fun Module.getTransitiveDependencies(includeTests: Boolean = false): Set<Module> {
    val result = mutableSetOf<Module>()
    val visited = mutableSetOf<Module>()
    
    fun collectDependencies(module: Module) {
        if (module in visited) return
        visited.add(module)
        
        module.getDependencies(includeTests).forEach { dependency ->
            result.add(dependency)
            collectDependencies(dependency)
        }
    }
    
    collectDependencies(this)
    return result
}

// ==================== 模块查找 ====================

/**
 * 在项目中查找包含指定包名的模块
 * 
 * @param packageName 包名，例如 "com.example.myapp"
 * @return 包含该包的模块列表
 */
fun Project.findModulesWithPackage(packageName: String): List<Module> {
    return getAllModules().filter { module ->
        module.sourceRoots.any { root ->
            val packagePath = packageName.replace('.', '/')
            root.findFileByRelativePath(packagePath) != null
        }
    }
}

/**
 * 查找包含指定文件路径模式的模块
 * 
 * @param pathPattern 路径模式，例如 "src/main/kotlin"
 * @return 匹配的模块列表
 */
fun Project.findModulesWithPathPattern(pathPattern: String): List<Module> {
    return getAllModules().filter { module ->
        module.contentRoots.any { root ->
            root.findFileByRelativePath(pathPattern) != null
        }
    }
}

// ==================== 模块分组 ====================

/**
 * 按模块名称前缀分组
 * 
 * 例如：将 "app-core", "app-web", "lib-common" 分组为：
 * - "app" -> ["app-core", "app-web"]
 * - "lib" -> ["lib-common"]
 * 
 * @param separator 分隔符，默认为 "-"
 * @return 分组后的 Map，key 为前缀，value 为模块列表
 */
fun Project.groupModulesByPrefix(separator: String = "-"): Map<String, List<Module>> {
    return getAllModules()
        .groupBy { module ->
            module.name.substringBefore(separator, module.name)
        }
}

/**
 * 获取叶子模块（没有被其他模块依赖的模块）
 * 
 * @return 叶子模块列表
 */
fun Project.getLeafModules(): List<Module> {
    val allModules = getAllModules()
    val dependedModules = allModules.flatMap { it.getDependencies(includeTests = true).toList() }.toSet()
    return allModules.filter { it !in dependedModules }
}

/**
 * 获取根模块（不依赖任何其他模块的模块）
 * 
 * @return 根模块列表
 */
fun Project.getRootModules(): List<Module> {
    return getAllModules().filter { module ->
        module.getDependencies(includeTests = false).isEmpty()
    }
}

// ==================== 模块路径 ====================

/**
 * 获取相对于项目根目录的模块路径
 * 
 * @return 相对路径，如果无法确定则返回 null
 */
val Module.relativePathToProject: String?
    get() {
        val projectBasePath = project.basePath ?: return null
        val modulePath = rootPath ?: return null
        return if (modulePath.startsWith(projectBasePath)) {
            modulePath.removePrefix(projectBasePath).removePrefix("/")
        } else {
            null
        }
    }

/**
 * 判断模块是否是顶层模块（直接位于项目根目录下）
 * 
 * @return true 表示是顶层模块，false 表示是嵌套模块
 */
val Module.isTopLevel: Boolean
    get() = relativePathToProject?.contains("/") == false

/**
 * 获取模块的父路径（模块所在的目录名）
 * 
 * @return 父路径，例如 "lib", "plugins" 等
 */
val Module.parentPath: String?
    get() = relativePathToProject?.substringBeforeLast("/", "")?.takeIf { it.isNotEmpty() }
