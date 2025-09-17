package site.addzero.inter

interface Platform {
    val name: String
}
interface PackageManager {
    val packageManagerPrefix: String
    fun installPackage(packageName: String): Boolean
    fun uninstallPackage(packageName: String): Boolean
    fun updatePackage(packageName: String): Boolean
    fun searchPackage(packageName: String): Boolean
}
interface IOUtils {
    fun createDirectory(path: String): Boolean
    fun deleteDirectory(path: String): Boolean
    fun copyFile(source: String, target: String): Boolean
    fun deleteFile(path: String): Boolean
    fun moveFile(source: String, target: String): Boolean
    fun readFile(path: String): String
    fun writeFile(path: String, content: String): Boolean
    fun appendFile(path: String, content: String): Boolean
    fun listFiles(path: String): List<String>
    fun fileExists(path: String): Boolean
}


expect fun getPlatform(): Platform
// 声明期望的家目录获取函数
expect fun getUserHomeDirectory(): String
