package site.addzero.util

import java.io.File

// 更完整的文件操作扩展函数
fun File.ensureExists(createParents: Boolean = true): File {
    if (!this.exists()) {
        if (createParents) {
            this.parentFile?.mkdirs()
        }
        if (this.isDirectory) {
            this.mkdirs()
        } else {
            this.createNewFile()
        }
    }
    return this
}

fun File.ensureNotExists(): File {
    if (this.exists()) {
        if (this.isDirectory) {
            this.deleteRecursively()
        } else {
            this.delete()
        }
    }
    return this
}

fun File.ensureDirectory(): File {
    if (exists()) {
        require(isDirectory) {
            "路径不是目录：$absolutePath"
        }
        return this
    }

    check(mkdirs() || exists()) {
        "无法创建目录：$absolutePath"
    }
    return this
}
