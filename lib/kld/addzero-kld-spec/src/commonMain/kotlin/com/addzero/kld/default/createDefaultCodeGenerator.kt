package com.addzero.kld.default

import com.addzero.kld.abs.KLOutputStream
import com.addzero.kld.processing.CodeGenerator
import com.addzero.kld.processing.Dependencies
import com.addzero.kld.symbol.KLClassDeclaration
import com.addzero.kld.symbol.KLFile

fun createDefaultCodeGenerator(): CodeGenerator {
    val generator = object : CodeGenerator {
        override fun createNewFile(
            dependencies: Dependencies,
            packageName: String,
            fileName: String,
            extensionName: String
        ): KLOutputStream {
            TODO("Not yet implemented")
        }

        override fun createNewFileByPath(
            dependencies: Dependencies,
            path: String,
            extensionName: String
        ): KLOutputStream {
            TODO("Not yet implemented")
        }

        override fun associate(
            sources: List<KLFile>,
            packageName: String,
            fileName: String,
            extensionName: String
        ) {
            TODO("Not yet implemented")
        }

        override fun associateByPath(
            sources: List<KLFile>,
            path: String,
            extensionName: String
        ) {
            TODO("Not yet implemented")
        }

        override fun associateWithClasses(
            classes: List<KLClassDeclaration>,
            packageName: String,
            fileName: String,
            extensionName: String
        ) {
            TODO("Not yet implemented")
        }

        override val generatedKLFile: Collection<KLFile>
            get() = TODO("Not yet implemented")

    }

    return generator
}
