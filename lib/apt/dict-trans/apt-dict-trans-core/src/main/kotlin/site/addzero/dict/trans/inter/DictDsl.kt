package site.addzero.dict.trans.inter

import site.addzero.dict.trans.inter.TransApi

/**
 * Base interface for generated DictDsl classes
 * Provides common translation functionality
 */
interface DictDsl<T> {
    /**
     * Perform dictionary translation
     */
    fun translate(transApi: TransApi): T

    /**
     * Check if this DSL supports the given class
     */
    fun supports(clazz: Class<*>): Boolean
}