package site.addzero.ksp.metadata.jimmer.entity.spi

data class JimmerEntityMeta(
    val qualifiedName: String,
    val packageName: String,
    val simpleName: String,
    val docComment: String = "",
    val properties: List<JimmerPropertyMeta> = emptyList()
)

data class JimmerPropertyMeta(
    val name: String,
    val type: JimmerTypeRef,
    val docComment: String = "",
    val formIgnored: Boolean = false
)

data class JimmerTypeRef(
    val qualifiedName: String? = null,
    val simpleName: String,
    val nullable: Boolean = false,
    val kind: JimmerTypeKind = JimmerTypeKind.OTHER,
    val typeArguments: List<JimmerTypeRef> = emptyList(),
    val sourceTypeName: String = simpleName
) {
    val packageName: String?
        get() = qualifiedName
            ?.substringBeforeLast('.', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
}

enum class JimmerTypeKind {
    BASIC,
    DATE_TIME,
    ENUM,
    ENTITY,
    COLLECTION,
    ARRAY,
    OTHER
}
