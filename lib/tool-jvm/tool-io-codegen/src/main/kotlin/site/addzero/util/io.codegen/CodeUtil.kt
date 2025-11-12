package site.addzero.util.io.codegen

import java.io.File

/**
 * 将包名转换为文件系统路径
 *
 * @param pkg 包名，例如 "site.addzero.xx"
 * @return 适配操作系统的路径分隔符的路径字符串
 */
fun pkgToPath(pkg: String): String {
    return pkg.replace(".", File.separator)
}


//// 构建路径
//val path = buildFilePath("/src/main/kotlin", "site.addzero.myapp", "", "Main", ".kt")
//
//// 生成代码文件
//genCodeWithPackage(
//    filePath = "/src/main/kotlin",
//    pkg = "site.addzero.myapp",
//    filePrefix = "",
//    fileName = "Main",
//    fileSuffix = ".kt",
//    code = "package site.addzero.myapp\n\nclass Main {\n    fun main() {\n        println(\"Hello World\")\n    }\n}",
//    skipExistFile = true
//)

/**
 * 构建完整文件路径
 *
 * @param filePath 基础文件路径
 * @param pkg 包名，例如 "site.addzero.xx"，会自动转换为路径分隔符
 * @param filePrefix 文件前缀
 * @param fileName 文件名
 * @param fileSuffix 文件后缀
 * @return 完整的文件路径
 */
fun buildFilePath(
    filePath: String,
    pkg: String,
    filePrefix: String,
    fileName: String,
    fileSuffix: String
): String {
    val normalizedPkg = pkgToPath(pkg)
    val basePath = if (filePath.endsWith(File.separator)) {
        filePath
    } else {
        "$filePath${File.separator}"
    }

    return "$basePath$normalizedPkg${File.separator}$filePrefix$fileName$fileSuffix"
}

/**
 * 生成代码文件
 *
 * @param pathname 文件路径
 * @param code 代码内容
 * @param skipExistFile 是否跳过已存在的文件，默认为false
 */
fun genCode(pathname: String, code: String, skipExistFile: Boolean = false) {
    val targetFile = File(pathname)
    targetFile.parentFile?.mkdirs()
    if (skipExistFile && targetFile.exists()) {
        return
    }
    targetFile.writeText(code)
}

/**
 * 根据包名等参数生成代码文件
 *
 * @param filePath 基础文件路径
 * @param pkg 包名，例如 "site.addzero.xx"
 * @param filePrefix 文件前缀
 * @param fileName 文件名
 * @param fileSuffix 文件后缀
 * @param code 代码内容
 * @param skipExistFile 是否跳过已存在的文件，默认为false
 */
fun genCodeWithPackage(
    filePath: String,
    pkg: String,
    filePrefix: String,
    fileName: String,
    fileSuffix: String,
    code: String,
    skipExistFile: Boolean = false
): String {
    val fullPath = buildFilePath(filePath, pkg, filePrefix, fileName, fileSuffix)
    genCode(fullPath, code, skipExistFile)
    return fullPath
}
