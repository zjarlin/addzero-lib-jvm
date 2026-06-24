package site.addzero.jimmer.ddl.compiler

import java.io.File

object JimmerDdlCompilerFiles {
    fun resolveOutputFile(settings: JimmerDdlCompilerSettings): File {
        val outputDir = File(settings.outputDir)
        return File(outputDir, settings.outputFileName)
    }

    fun writeOutputFile(
        settings: JimmerDdlCompilerSettings,
        sql: String,
    ): File {
        val outputFile = resolveOutputFile(settings)
        outputFile.parentFile.mkdirs()
        outputFile.writeText(sql)
        return outputFile
    }
}
