package site.addzero.util.lsi.file

import site.addzero.util.lsi.clazz.LsiClass

/**
 * LsiFile 通用工具函数
 * 
 * 这些函数可以在任何 LsiFile 实现（PSI, Kotlin PSI, Reflection）中复用
 */

// ==================== 文件信息 ====================

/**
 * 获取文件名（不含扩展名）
 * 
 * @return 文件名，例如 "UserService"
 */
val LsiFile.nameWithoutExtension: String
    get() = name.substringBeforeLast('.')

/**
 * 获取文件扩展名
 * 
 * @return 扩展名，例如 "kt", "java"
 */
val LsiFile.extension: String
    get() = name.substringAfterLast('.', "")

/**
 * 判断是否为 Java 文件
 */
val LsiFile.isJavaFile: Boolean
    get() = extension == "java"

/**
 * 判断是否为 Kotlin 文件
 */
val LsiFile.isKotlinFile: Boolean
    get() = extension == "kt"

/**
 * 获取文件目录路径
 * 
 * @return 目录路径，例如 "/path/to/project/src/main/kotlin/com/example"
 */
val LsiFile.directoryPath: String?
    get() = filePath?.substringBeforeLast('/')

/**
 * 获取相对于源码根目录的路径
 * 
 * 例如：从 "/project/src/main/kotlin/com/example/User.kt" 
 * 提取 "com/example/User.kt"
 * 
 * @return 相对路径，如果无法确定则返回 null
 */
fun LsiFile.getRelativePathFromSourceRoot(): String? {
    val path = filePath ?: return null
    val pkg = packageName ?: return null
    
    // 将包名转换为路径
    val packagePath = pkg.replace('.', '/')
    
    // 查找包路径在文件路径中的位置
    val index = path.indexOf(packagePath)
    if (index == -1) return null
    
    return path.substring(index)
}

// ==================== 类查找 ====================

/**
 * 判断文件是否包含指定名称的类
 * 
 * @param className 类名
 * @return true 表示包含，false 表示不包含
 */
fun LsiFile.hasClass(className: String): Boolean {
    return findClassByName(className) != null
}

/**
 * 判断文件是否包含多个类
 * 
 * @return true 表示包含多个类，false 表示只有一个或没有类
 */
val LsiFile.hasMultipleClasses: Boolean
    get() = classes.size > 1

/**
 * 判断文件是否为空（没有类定义）
 * 
 * @return true 表示没有类，false 表示有类
 */
val LsiFile.isEmpty: Boolean
    get() = classes.isEmpty()

/**
 * 获取主类（通常是与文件名相同的类）
 * 
 * @return 主类，如果没有则返回第一个类
 */
val LsiFile.mainClass: LsiClass?
    get() {
        // 优先返回文件名匹配的类
        val fileNameClass = findClassByName(nameWithoutExtension)
        if (fileNameClass != null) return fileNameClass
        
        // 否则返回第一个public类
        val publicClass = classes.firstOrNull { !it.isInterface && !it.isEnum }
        if (publicClass != null) return publicClass
        
        // 最后返回第一个类
        return classes.firstOrNull()
    }

/**
 * 查找所有 POJO 类
 * 
 * @return POJO 类列表
 */
val LsiFile.pojoClasses: List<LsiClass>
    get() = classes.filter { it.isPojo }

/**
 * 判断文件是否包含 POJO 类
 * 
 * @return true 表示包含 POJO 类，false 表示不包含
 */
val LsiFile.hasPojoClass: Boolean
    get() = classes.any { it.isPojo }

// ==================== 包名操作 ====================

/**
 * 获取父包名
 * 
 * 例如：从 "com.example.service" 获取 "com.example"
 * 
 * @return 父包名，如果是顶层包则返回空字符串
 */
val LsiFile.parentPackageName: String
    get() = packageName?.substringBeforeLast('.', "") ?: ""

/**
 * 获取包的最后一部分
 * 
 * 例如：从 "com.example.service" 获取 "service"
 * 
 * @return 包名的最后部分
 */
val LsiFile.packageSimpleName: String
    get() = packageName?.substringAfterLast('.', "") ?: ""

/**
 * 判断是否在指定包下（包括子包）
 * 
 * @param packagePrefix 包前缀，例如 "com.example"
 * @return true 表示在该包或其子包下
 */
fun LsiFile.isInPackage(packagePrefix: String): Boolean {
    val pkg = packageName ?: return false
    return pkg == packagePrefix || pkg.startsWith("$packagePrefix.")
}

/**
 * 获取包的深度
 * 
 * 例如："com.example.service" 的深度为 3
 * 
 * @return 包深度，如果没有包则返回 0
 */
val LsiFile.packageDepth: Int
    get() {
        val pkg = packageName
        if (pkg.isNullOrEmpty()) return 0
        return pkg.count { it == '.' } + 1
    }

// ==================== 注解判断 ====================

/**
 * 判断文件是否有指定的注解
 * 
 * @param annotationName 注解的简单名称或全限定名
 * @return true 表示有该注解，false 表示没有
 */
fun LsiFile.hasAnnotation(annotationName: String): Boolean {
    return annotations.any { annotation ->
        annotation.simpleName == annotationName || 
        annotation.qualifiedName == annotationName
    }
}

/**
 * 查找指定名称的注解
 * 
 * @param annotationName 注解的简单名称或全限定名
 * @return 注解列表
 */
fun LsiFile.findAnnotations(annotationName: String) = annotations.filter { annotation ->
    annotation.simpleName == annotationName || 
    annotation.qualifiedName == annotationName
}

// ==================== 类型判断 ====================

/**
 * 判断文件是否为测试文件
 * 
 * 基于文件名或包名判断（例如：文件名包含 "Test" 或在 test 包下）
 * 
 * @return true 表示是测试文件，false 表示不是
 */
val LsiFile.isTestFile: Boolean
    get() {
        // 文件名包含 Test
        if (nameWithoutExtension.endsWith("Test") || 
            nameWithoutExtension.endsWith("Tests") ||
            nameWithoutExtension.startsWith("Test")) {
            return true
        }
        
        // 路径包含 test 或 tests
        val path = filePath?.lowercase()
        if (path != null && (path.contains("/test/") || path.contains("/tests/"))) {
            return true
        }
        
        return false
    }

/**
 * 判断文件是否为接口定义文件
 * 
 * @return true 表示文件中所有类都是接口
 */
val LsiFile.isInterfaceFile: Boolean
    get() = classes.isNotEmpty() && classes.all { it.isInterface }

/**
 * 判断文件是否为枚举定义文件
 * 
 * @return true 表示文件中所有类都是枚举
 */
val LsiFile.isEnumFile: Boolean
    get() = classes.isNotEmpty() && classes.all { it.isEnum }

// ==================== 统计信息 ====================

/**
 * 统计文件中的类数量
 */
val LsiFile.classCount: Int
    get() = classes.size

/**
 * 统计文件中的接口数量
 */
val LsiFile.interfaceCount: Int
    get() = classes.count { it.isInterface }

/**
 * 统计文件中的枚举数量
 */
val LsiFile.enumCount: Int
    get() = classes.count { it.isEnum }

/**
 * 统计文件中的 POJO 类数量
 */
val LsiFile.pojoCount: Int
    get() = classes.count { it.isPojo }

// ==================== 字符串表示 ====================

/**
 * 获取文件的描述性字符串
 * 
 * @return 描述字符串，例如 "UserService.kt (com.example.service, 3 classes)"
 */
fun LsiFile.toDescriptiveString(): String {
    val pkg = packageName ?: "default package"
    val classCount = classes.size
    return "$name ($pkg, $classCount ${if (classCount == 1) "class" else "classes"})"
}

/**
 * 获取文件的完整限定名
 * 
 * @return 完整限定名，例如 "com.example.service.UserService"
 */
fun LsiFile.getFullyQualifiedName(): String? {
    val pkg = packageName
    val fileName = nameWithoutExtension
    return if (!pkg.isNullOrEmpty()) {
        "$pkg.$fileName"
    } else {
        fileName
    }
}
