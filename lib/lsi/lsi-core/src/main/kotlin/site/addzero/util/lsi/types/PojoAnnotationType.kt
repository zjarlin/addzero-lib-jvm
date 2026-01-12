package site.addzero.util.lsi.types

/**
 * POJO相关注解的强类型枚举
 *
 * 提供类型安全的注解表示，替代散落在各处的魔法字符串
 */
enum class PojoAnnotationType(
    val fqName: String,
    val simpleName: String,
    val category: PojoAnnotationCategory
) {
    // Entity注解
    JPA_ENTITY("javax.persistence.Entity", "Entity", PojoAnnotationCategory.ENTITY),
    JAKARTA_ENTITY("jakarta.persistence.Entity", "Entity", PojoAnnotationCategory.ENTITY),
    JIMMER_ENTITY("org.babyfish.jimmer.sql.Entity", "Entity", PojoAnnotationCategory.ENTITY),
    JPA_MAPPED_SUPERCLASS("javax.persistence.MappedSuperclass", "MappedSuperclass", PojoAnnotationCategory.ENTITY),
    JAKARTA_MAPPED_SUPERCLASS("jakarta.persistence.MappedSuperclass", "MappedSuperclass", PojoAnnotationCategory.ENTITY),
    JIMMER_MAPPED_SUPERCLASS("org.babyfish.jimmer.sql.MappedSuperclass", "MappedSuperclass", PojoAnnotationCategory.ENTITY),

    // Table注解
    MYBATIS_PLUS_TABLE("com.baomidou.mybatisplus.annotation.TableName", "TableName", PojoAnnotationCategory.TABLE),
    JIMMER_TABLE("org.babyfish.jimmer.sql.Table", "Table", PojoAnnotationCategory.TABLE),
    JPA_TABLE("javax.persistence.Table", "Table", PojoAnnotationCategory.TABLE),
    JAKARTA_TABLE("jakarta.persistence.Table", "Table", PojoAnnotationCategory.TABLE),

    // Lombok注解
    LOMBOK_DATA("lombok.Data", "Data", PojoAnnotationCategory.LOMBOK),
    LOMBOK_GETTER("lombok.Getter", "Getter", PojoAnnotationCategory.LOMBOK),
    LOMBOK_SETTER("lombok.Setter", "Setter", PojoAnnotationCategory.LOMBOK);

    companion object {
        private val byFqName = entries.associateBy { it.fqName }
        private val byShortName = entries.groupBy { it.simpleName }
        private val byCategory = entries.groupBy { it.category }

        fun findByFqName(fqName: String): PojoAnnotationType? = byFqName[fqName]
        fun findByShortName(shortName: String): List<PojoAnnotationType> = byShortName[shortName].orEmpty()
        fun isPojoAnnotation(fqName: String): Boolean = fqName in byFqName
        
        fun allInCategory(category: PojoAnnotationCategory): List<PojoAnnotationType> = 
            byCategory[category].orEmpty()
        
        val allEntityAnnotations: List<PojoAnnotationType> get() = allInCategory(PojoAnnotationCategory.ENTITY)
        val allTableAnnotations: List<PojoAnnotationType> get() = allInCategory(PojoAnnotationCategory.TABLE)
        val allLombokAnnotations: List<PojoAnnotationType> get() = allInCategory(PojoAnnotationCategory.LOMBOK)
        
        val allFqNames: Set<String> get() = byFqName.keys
        val allShortNames: Set<String> get() = byShortName.keys
    }
}

/**
 * POJO注解的分类
 */
enum class PojoAnnotationCategory {
    /** 实体注解 (Entity, MappedSuperclass) */
    ENTITY,
    /** 表名注解 (Table, TableName) */
    TABLE,
    /** Lombok注解 (Data, Getter, Setter) */
    LOMBOK
}
