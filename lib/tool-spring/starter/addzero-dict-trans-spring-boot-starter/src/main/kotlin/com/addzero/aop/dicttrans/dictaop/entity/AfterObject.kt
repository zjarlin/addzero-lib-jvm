package com.addzero.aop.dicttrans.dictaop.entity

import com.addzero.aop.dicttrans.anno.Dict


/**
 * @author zjarlin
 * @since 2023/10/11 12:09
 */
data class AfterObject(
    var afterObject: Any,

    var describetors: MutableList<Describetor<Dict>>,

    //        Map<String, List<Dict>> annoMap;
    var needAddField: MutableSet<Pair<String, Class<*>>>
)
