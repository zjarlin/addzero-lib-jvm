package site.addzero.aop.dicttrans.dictaop.entity

import java.lang.reflect.Type


/**
 * @author zjarlin
 * @since 2023/6/20 09:44
 */
class Describetor<A : Annotation?> {
    /**
     * 父类属性类型是T还是COllT
     */
    private val superObjectFieldTypeEnum: String? = null

    /**
     * 父类属性名称
     */
    private val superObjectFieldName: String? = null

    /**
     * 上级对象,用于处理嵌套类型,为null说明是当前root对象
     */
    private val superObject: Any? = null

    /**
     * 需要递归的属性枚举,常规属性,T,Collection需递归处理
     */
    private val fieldEnum: String? = null


    private val rootObject: Any? = null
    private val rootObjectClass: Class<*>? = null


    private val fieldName: String? = null
    private val fieldValue: Any? = null
    private val fieldType: Type? = null
    private val needSearchAnnotation: A? = null

    private val needSearchAnnotations: MutableList<A?>? = null
    private val annotations: Array<Annotation?>?=null
}
