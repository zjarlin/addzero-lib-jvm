package com.addzero.aop.dicttrans.dictaop.entity

/**
 * @author zjarlin
 * @since 2023/10/11 16:52
 */
data   class TransInfo<A : Annotation> (
    /**
     * 父类属性类型是T还是COllT
     */
    val superObjectFieldTypeEnum: String?,

    /**
     * 父类属性名称
     */
    val superObjectFieldName: String? = null,

    /**
     * 上级对象,用于处理嵌套类型,为null说明是当前root对象
     */
    val superObject: Any?,

    /**
     * 需要递归的属性枚举,常规属性,T,Collection需递归处理
     */
    val fieldEnum: String?,

    /**
     * 翻译的注解
     */
    val anno: A,

    val translationProcess: ((Any) -> Any)?,

    val rootObject: Any,

    /**
     * 翻译后的对象
     */
    val afterObject: Any?,

    /**
     * 翻译后的对象字节码
     */
    val afterObjectClass: Any?,

    /**
     * 翻译后的属性名
     */
    val translatedAttributeNames: String,

    /**
     * 翻译前的属性名
     */
    val attributeNameBeforeTranslation: String,

    /**
     * 翻译前的值
     */
    val valueBeforeTranslation: Any,

    /**
     * 翻译后的值
     */
    var translatedValue: Any?,

    /**
     * 翻译后的类型
     */
    val translatedType: Class<*>,

    /**
     * 翻译的分类
     */
    val classificationOfTranslation: Int?,

    /**
     * 根据rootObject和翻译后的属性名生成的唯一标识码
     */
    val rootObjectHashBsm: String?,
)
