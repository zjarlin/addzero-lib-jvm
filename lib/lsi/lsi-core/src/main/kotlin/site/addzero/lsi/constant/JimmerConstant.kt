package site.addzero.lsi.constant

/**
 * Jimmer 相关常量
 */

/**
 * Jimmer @Column 注解的全限定名
 */
const val JIMMER_COLUMN_FQ_NAME = "org.babyfish.jimmer.meta.annotation.Column"

/**
 * MyBatis Plus @TableField 注解的全限定名
 */
const val MP_TABLE_FIELD_FQ_NAME = "com.baomidou.mybatisplus.annotation.TableField"

/**
 * 列名注解的方法名映射
 * Key: 注解全限定名
 * Value: 获取列名的方法名
 */
val COLUMN_NAME_ANNOTATION_METHOD_MAP_JIMMER = mapOf(
    JIMMER_COLUMN_FQ_NAME to "name",
    MP_TABLE_FIELD_FQ_NAME to "value"
)
