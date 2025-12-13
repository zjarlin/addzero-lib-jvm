package site.addzero.apt.dict.processor

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import java.io.File
import java.security.MessageDigest

/**
 * Incremental compilation support for APT processor
 * 
 * This class provides functionality to:
 * 1. Track changes in annotated classes
 * 2. Determine if regeneration is needed
 * 3. Cache compilation results
 * 4. Support incremental builds
 */
class IncrementalCompilationSupport(
    private val processingEnv: ProcessingEnvironment
) {
    
    private val cacheDir = File(System.getProperty("java.io.tmpdir"), "apt-dict-cache")
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Checks if the given class needs to be reprocessed
     * 
     * @param typeElement The class element to check
     * @param dictFields The current dictionary fields
     * @return true if reprocessing is needed, false otherwise
     */
    fun needsReprocessing(typeElement: TypeElement, dictFields: List<DictFieldInfo>): Boolean {
        val className = typeElement.qualifiedName.toString()
        val currentHash = calculateClassHash(typeElement, dictFields)
        val cachedHash = getCachedHash(className)
        
        return currentHash != cachedHash
    }
    
    /**
     * Updates the cache with the current class hash
     */
    fun updateCache(typeElement: TypeElement, dictFields: List<DictFieldInfo>) {
        val className = typeElement.qualifiedName.toString()
        val currentHash = calculateClassHash(typeElement, dictFields)
        setCachedHash(className, currentHash)
    }
    
    /**
     * Calculates a hash for the class and its dictionary fields
     * This hash is used to determine if the class has changed
     */
    private fun calculateClassHash(typeElement: TypeElement, dictFields: List<DictFieldInfo>): String {
        val content = StringBuilder()
        
        // Include class name and modifiers
        content.append(typeElement.qualifiedName.toString())
        content.append(typeElement.modifiers.toString())
        
        // Include all field information
        dictFields.forEach { field ->
            content.append(field.sourceField)
            content.append(field.targetField)
            content.append(field.dictCode)
            content.append(field.table)
            content.append(field.codeColumn)
            content.append(field.nameColumn)
            content.append(field.spelExp)
            content.append(field.condition)
        }
        
        // Calculate MD5 hash
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(content.toString().toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Gets the cached hash for a class
     */
    private fun getCachedHash(className: String): String? {
        val cacheFile = File(cacheDir, "${className.replace(".", "_")}.hash")
        return if (cacheFile.exists()) {
            try {
                cacheFile.readText().trim()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Sets the cached hash for a class
     */
    private fun setCachedHash(className: String, hash: String) {
        val cacheFile = File(cacheDir, "${className.replace(".", "_")}.hash")
        try {
            cacheFile.writeText(hash)
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "Failed to update cache for $className: ${e.message}"
            )
        }
    }
    
    /**
     * Clears the entire cache
     */
    fun clearCache() {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "Failed to clear cache: ${e.message}"
            )
        }
    }
    
    /**
     * Gets cache statistics
     */
    fun getCacheStatistics(): CacheStatistics {
        val cacheFiles = cacheDir.listFiles() ?: emptyArray()
        return CacheStatistics(
            totalCachedClasses = cacheFiles.size,
            cacheDirectory = cacheDir.absolutePath,
            cacheSize = cacheFiles.sumOf { it.length() }
        )
    }
}

/**
 * Cache statistics for incremental compilation
 */
data class CacheStatistics(
    val totalCachedClasses: Int,
    val cacheDirectory: String,
    val cacheSize: Long
) {
    override fun toString(): String {
        return "CacheStatistics(classes=$totalCachedClasses, size=${cacheSize}B, dir=$cacheDirectory)"
    }
}