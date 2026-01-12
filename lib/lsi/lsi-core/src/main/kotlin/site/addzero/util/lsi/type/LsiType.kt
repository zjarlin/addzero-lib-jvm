package site.addzero.util.lsi.type

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.isNullable
import site.addzero.util.lsi.clazz.LsiClass

/**
 * 语言无关的类型结构抽象接口
 * Lsi = Language Structure Interface
 */
interface LsiType {
    /**
     * 获取类型的名称
     */
    val name: String?

    /**
     * 获取类型的完全限定名
     */
    val qualifiedName: String?

    /**
     * 获取类型的可读文本表示 此为泛型设计 例如拿到User<GenericType1,GenericType2> 这样的字符串,比较复杂,先简单实现
     */
    val presentableText: String?

    /**
     * 获取类型的注解
     */
    val annotations: List<LsiAnnotation>

    /**
     * 判断是否为集合类型
     */
    val isCollectionType: Boolean

    /**
     * 判断是否为可空类型
     * 默认根据注解判断：有 @Nullable 返回 true，有 @NonNull 返回 false，否则保守策略返回 true
     */
    val isNullable: Boolean
        get() {
            if (isPrimitive) {
                return false
            }
            return annotations.isNullable()
        }

    /**
     * 获取泛型参数类型列表
     */
    val typeParameters: List<LsiType>

    /**
     * 判断是否为原始类型
     */
    val isPrimitive: Boolean

    /**
     * 获取数组元素类型（如果是数组类型）
     */
    val componentType: LsiType?

    /**
     * 判断是否为数组类型
     */
    val isArray: Boolean

    /**
     * 获取类型对应的LsiClass（如果是类类型）
     */
    val lsiClass: LsiClass?



}
