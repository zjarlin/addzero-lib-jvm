package site.addzero.apt.dict.annotations

/**
 * Marks a class for compile-time dictionary translation enhancement.
 * 
 * This annotation triggers the APT processor to generate an enhanced version
 * of the class with dictionary translation capabilities.
 * 
 * Usage:
 * ```kotlin
 * @Dict
 * data class UserVO(
 *     val id: Long,
 *     @DictField(dictCode = "user_status") 
 *     val status: String,
 *     val statusText: String? = null // Will be populated by translation
 * )
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Dict(
    /**
     * Suffix for the generated enhanced class name
     */
    val suffix: String = "Enhanced",
    
    /**
     * Whether to generate extension methods
     */
    val generateExtensions: Boolean = true,
    
    /**
     * Whether to generate builder pattern
     */
    val generateBuilder: Boolean = false
)

/**
 * Marks a field for dictionary translation.
 * 
 * This annotation specifies how a field should be translated using
 * system dictionaries, table dictionaries, or SPEL expressions.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class DictField(
    /**
     * System dictionary code for translation
     */
    val dictCode: String = "",
    
    /**
     * Target field name for translated value
     */
    val targetField: String = "",
    
    /**
     * Table name for table dictionary translation
     */
    val table: String = "",
    
    /**
     * Code column name for table dictionary
     */
    val codeColumn: String = "code",
    
    /**
     * Name column name for table dictionary
     */
    val nameColumn: String = "name",
    
    /**
     * Additional WHERE condition for table dictionary
     */
    val condition: String = "",
    
    /**
     * SPEL expression for dynamic translation
     */
    val spelExp: String = "",
    
    /**
     * Whether to ignore null values
     */
    val ignoreNull: Boolean = true,
    
    /**
     * Default value when translation fails
     */
    val defaultValue: String = "",
    
    /**
     * Whether to cache translation results
     */
    val cached: Boolean = true
)