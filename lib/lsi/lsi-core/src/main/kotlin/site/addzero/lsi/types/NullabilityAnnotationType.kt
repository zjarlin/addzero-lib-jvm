package site.addzero.lsi.types

/**
 * 可空性注解的强类型枚举
 *
 * 处理各种可空性标记注解 (Nullable, NonNull等)
 */
enum class NullabilityAnnotationType(
    val simpleName: String,
    val nullability: Nullability
) {
    // Nullable注解
    NULLABLE("Nullable", Nullability.NULLABLE),
    CHECK_FOR_NULL("CheckForNull", Nullability.NULLABLE),

    // NonNull注解
    NON_NULL("NonNull", Nullability.NON_NULL),
    NONNULL("Nonnull", Nullability.NON_NULL),
    NOT_NULL("NotNull", Nullability.NON_NULL),

    // Package level
    NULL_MARKED("NullMarked", Nullability.NULL_MARKED),
    NULL_UNMARKED("NullUnmarked", Nullability.NULL_UNMARKED);

    companion object {
        private val bySimpleName = entries.associateBy { it.simpleName }
        private val byNullability = entries.groupBy { it.nullability }

        fun findBySimpleName(simpleName: String): NullabilityAnnotationType? = bySimpleName[simpleName]

        fun allOfNullability(nullability: Nullability): List<NullabilityAnnotationType> =
            byNullability[nullability].orEmpty()

        val allNullableAnnotations: List<NullabilityAnnotationType> get() =
            allOfNullability(Nullability.NULLABLE)
        val allNonNullAnnotations: List<NullabilityAnnotationType> get() =
            allOfNullability(Nullability.NON_NULL)

        val allNullableSimpleNames: Set<String> get() =
            allNullableAnnotations.map { it.simpleName }.toSet()
        val allNonNullSimpleNames: Set<String> get() =
            allNonNullAnnotations.map { it.simpleName }.toSet()
    }
}

/**
 * 可空性类别
 */
enum class Nullability {
    /** 可空 */
    NULLABLE,
    /** 非空 */
    NON_NULL,
    /** 包级别标记为可能包含null (JSpecify) */
    NULL_MARKED,
    /** 包级别标记为不包含null (JSpecify) */
    NULL_UNMARKED
}
