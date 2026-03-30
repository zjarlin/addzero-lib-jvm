package site.addzero.ksp.metadata.jimmer.entity.spi

import java.io.File

object JimmerGeneratedSourceWriter {
    fun writeKotlinFile(
        rootOutputDir: String,
        packageName: String,
        fileName: String,
        content: String
    ): File {
        val packageDir = packageName.replace('.', File.separatorChar)
        val normalizedRoot = File(rootOutputDir).path.trimEnd(File.separatorChar)
        val normalizedPackageDir = packageDir.trimEnd(File.separatorChar)
        val baseDir = if (
            normalizedPackageDir.isNotBlank() &&
            normalizedRoot.endsWith(normalizedPackageDir)
        ) {
            File(rootOutputDir)
        } else {
            File(rootOutputDir, packageDir)
        }
        val outputFile = File(baseDir, fileName)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(content)
        return outputFile
    }
}
