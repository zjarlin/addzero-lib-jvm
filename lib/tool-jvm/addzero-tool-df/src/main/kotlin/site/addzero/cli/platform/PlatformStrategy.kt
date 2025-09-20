package site.addzero.cli.platform

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

interface PlatformStrategy {
    val support: Boolean

    /**
     * 读取用户输入的一行文本
     * @return 用户输入的文本，如果发生错误则返回null
     */
    fun readLine(): String? {
        return kotlin.io.readLine()?.trim()
    }

    fun readLine(prompt: String): String? {
        println(prompt)
        return readLine()
    }


    fun readYesNo(prompt: String): Boolean? {
        while (true) {
            println("$prompt  (y/n)，默认y:")
            val input = readLine("$prompt  (y/n)，默认y:")?.lowercase()
            when (input) {
                "y", "yes", "", null -> return true
                "n", "no" -> return false
                else -> println("Invalid input. Please enter 'y' or 'n'.")
            }
        }
    }


    /**
     * 获取可用的处理器数量
     * @return 处理器数量
     */
    fun getAvailableProcessors(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    /**
     * 退出进程
     * @param status 退出状态码
     */
    fun exitProcess(status: Int): Nothing {
        return kotlin.system.exitProcess(status)
    }


    /**
     * 创建目录
     * @param path 目录路径
     * @return 是否创建成功
     */
    fun mkdir(path: String): Boolean {
        return try {
            File(path).mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容
     */
    fun readFile(path: String): String {
        return File(path).readText()
    }

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param content 文件内容
     * @return 是否写入成功
     */
    fun writeFile(path: String, content: String): Boolean {
        return try {
            File(path).writeText(content)
            true
        } catch (e: Exception) {
            false
        }

    }

    /**
     * 获取环境变量
     * @param name 环境变量名称
     * @return 环境变量值，如果不存在则返回null
     */
    fun getEnv(name: String): String? {
        return System.getenv(name)
    }

    /**
     * 设置环境变量
     * @param name 环境变量名称
     * @param value 环境变量值
     */
    fun setEnv(name: String, value: String)

    /**
     * 获取用户主目录
     * @return 用户主目录路径
     */
    fun getHomeDir(): String {
        return System.getProperty("user.home")
    }

    /**
     * 判断路径是否为目录
     * @param path 路径
     * @return 是否为目录
     */
    fun isDirectory(path: String): Boolean {
        return File(path).isDirectory
    }

    /**
     * 执行命令
     * @param command 命令
     * @param timeout 超时时间（毫秒）
     * @return Pair<退出码, 输出内容>
     */
    fun executeCommand(command: String, timeout: Long = 99999): CommandResult

    /**
     * 获取当前平台类型
     * @return 平台类型
     */
    fun getPlatformType(): PlatformType

    // 以下为通用默认实现

    fun createSymlink(absolutePath: String, linkPath: String): Boolean {
        return ProUtil.createSymlink(absolutePath, linkPath)
    }






    private fun createLinkAndVerify(source: File, target: File): Boolean {
        // 确保源位置没有残留文件
        if (source.exists() && !source.delete()) {
            System.err.println("错误：无法删除源位置残留文件 - ${source.absolutePath}")
            return false
        }

        // 创建软链接
        return try {
            Files.createSymbolicLink(source.toPath(), target.toPath())
            // 验证软链接
            val linkTarget = Files.readSymbolicLink(source.toPath()).toFile().canonicalFile
            if (linkTarget == target) {
                println("成功：文件已移动并创建软链接 - 源：${source.absolutePath} -> 目标：${target.absolutePath}")
                true
            } else {
                System.err.println("错误：软链接验证失败 - 实际指向 ${linkTarget.absolutePath}，预期 ${target.absolutePath}")
                source.delete() // 清理无效链接
                false
            }
        } catch (e: Exception) {
            System.err.println("错误：创建软链接失败 - ${e.message ?: "未知错误"}")
            false
        }
    }







    /**
     * 幂等地将软链接替换为其指向的真实文件/目录
     * 特性：多次执行结果相同，非软链接路径不做处理
     *
     * @param linkPath 软链接路径
     * @return 操作是否实际执行（true：执行了替换，false：无需操作）
     */
    fun undomvln(linkPath: String): Boolean {
        val path = Paths.get(linkPath)
        return undomvln(path)
    }

    /**
     * 幂等地将软链接替换为其指向的真实文件/目录（Path重载）
     */
    @OptIn(ExperimentalPathApi::class)
    fun undomvln(linkPath: Path): Boolean {
        // 1. 检查路径是否存在
        if (!linkPath.exists()) {
            return false // 路径不存在，无需操作（幂等性保证）
        }

        // 2. 检查是否为软链接
        if (!Files.isSymbolicLink(linkPath)) {
            return false // 不是软链接，无需操作
        }

        // 3. 获取软链接指向的目标路径（绝对路径）
        val targetPath = Files.readSymbolicLink(linkPath).toAbsolutePath()
        if (!targetPath.exists()) {
            System.err.println("错误：软链接指向的目标不存在 - $targetPath")
        }

        // 4. 创建临时目录用于中转（避免复制过程中冲突）
        val tempDir = Files.createTempDirectory("symlink-replace-")
        val tempTargetCopy = tempDir.resolve(targetPath.fileName)

        try {
            // 5. 复制目标文件/目录到临时位置
            copyRecursively(targetPath, tempTargetCopy)

            // 6. 删除原软链接
            linkPath.deleteExisting()

            // 7. 将临时复制的文件移动到原软链接位置
            Files.move(
                tempTargetCopy,
                linkPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )

            return true
        } finally {
            // 清理临时目录
            tempDir.deleteRecursively()
        }
    }

    /**
     * 递归复制文件/目录
     */
    private fun copyRecursively(source: Path, target: Path) {
        when {
            source.isDirectory() -> {
                // 创建目标目录
                target.createDirectories()

                // 复制目录内容
                Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        val relativePath = source.relativize(file)
                        val targetFile = target.resolve(relativePath)
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING)
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                        val relativePath = source.relativize(dir)
                        val targetDir = target.resolve(relativePath)
                        targetDir.createDirectories()
                        return FileVisitResult.CONTINUE
                    }
                })
            }
            source.isRegularFile() -> {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
            else -> {
                throw UnsupportedOperationException("不支持的文件类型: ${source.fileName}")
            }
        }
    }

    // 使用示例
    fun main() {
        val symlinkPath = "/path/to/symlink" // 软链接路径

        try {
            val executed = undomvln(symlinkPath)
            if (executed) {
                println("软链接已替换为真实文件")
            } else {
                println("无需操作（路径不存在或不是软链接）")
            }
        } catch (e: Exception) {
            println("操作失败: ${e.message}")
        }
    }



}
