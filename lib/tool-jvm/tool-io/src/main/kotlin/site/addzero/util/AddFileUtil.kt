package site.addzero.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object AddFileUtil {
    /**
     * 将文件或目录从源路径移动到目标路径，并在原位置创建软链接
     * @param sourcePath 源路径（位置1）
     * @param targetPath 目标路径（位置2）
     * @return 操作成功返回true，失败返回false
     */
    fun mvln(sourcePath: String, targetPath: String): Boolean {
        val sourceFile = File(sourcePath)
        val targetFile = File(targetPath)

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            System.err.println("源文件不存在: $sourcePath")
            return false
        }

        // 如果源路径和目标路径相同，则不执行任何操作
        if (sourcePath == targetPath) {
            return true
        }

        try {
            // 智能处理目标路径：如果目标路径不以源文件名结尾，则添加源文件名
            val finalTargetFile = if (targetFile.name != sourceFile.name) {
                File(targetFile, sourceFile.name)
            } else {
                targetFile
            }

            // 确保目标目录存在
            finalTargetFile.parentFile?.mkdirs()

            // 移动文件或目录
            val movedFile = sourceFile.renameTo(finalTargetFile)

            if (movedFile) {
                // 创建软链接
                if (finalTargetFile.isDirectory) {
                    // 如果是目录，创建指向目录的软链接
                    Files.createSymbolicLink(
                        Paths.get(sourcePath),
                        finalTargetFile.toPath()
                    )
                } else {
                    // 如果是文件，创建指向文件的软链接
                    Files.createSymbolicLink(
                        Paths.get(sourcePath),
                        finalTargetFile.toPath()
                    )
                }
                println("成功移动 $sourcePath 到 ${finalTargetFile.absolutePath} 并创建软链接")
                return true
            } else {
                System.err.println("移动失败: $sourcePath")
                return false
            }
        } catch (e: Exception) {
            System.err.println("操作失败: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Undo操作：将软链接指向的文件或目录移回原位置，并删除软链接
     * @param symlinkPath 软链接路径（如 /Users/zjarlin/testdot）
     * @return 操作成功返回true，失败返回false
     */
    fun undoMvln(symlinkPath: String): Boolean {
        val symlinkFile = File(symlinkPath)

        // 检查文件是否存在
        if (!symlinkFile.exists()) {
            System.err.println("文件不存在: $symlinkPath")
            return false
        }

        // 检查是否为软链接
        if (!Files.isSymbolicLink(symlinkFile.toPath())) {
            System.err.println("不是软链接: $symlinkPath")
            return false
        }

        try {
            // 获取软链接指向的目标路径
            val targetPath: Path = Files.readSymbolicLink(symlinkFile.toPath())
            val resolvedTargetPath = if (targetPath.isAbsolute) {
                targetPath
            } else {
                // 如果是相对路径，需要解析为绝对路径
                symlinkFile.toPath().parent.resolve(targetPath).normalize()
            }

            val targetFile = resolvedTargetPath.toFile()

            // 检查目标文件是否存在
            if (!targetFile.exists()) {
                System.err.println("软链接指向的目标文件不存在: $resolvedTargetPath")
                return false
            }

            // 先删除软链接
            symlinkFile.delete()

            // 移动文件或目录回原位置
            val moved = targetFile.renameTo(symlinkFile)

            if (moved) {
                println("成功将 ${resolvedTargetPath} 移回 $symlinkPath")
                return true
            } else {
                System.err.println("移动失败")
                return false
            }
        } catch (e: Exception) {
            System.err.println("操作失败: ${e.message}")
            e.printStackTrace()
            return false
        }
    }


}
