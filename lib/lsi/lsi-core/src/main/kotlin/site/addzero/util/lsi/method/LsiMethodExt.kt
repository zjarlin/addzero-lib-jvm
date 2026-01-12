package site.addzero.util.lsi.method

import site.addzero.util.lsi.clazz.LsiClass

/**
 * 检查方法是否具有指定的注解
 * @param annotationNames 注解全限定名数组
 * @return 如果方法具有其中任何一个注解，则返回true，否则返回false
 */
fun LsiMethod.hasAnnotation(vararg annotationNames: String): Boolean {
    return annotationNames.any { annotationName ->
        annotations.any { annotation ->
            annotation.qualifiedName == annotationName
        }
    }
}

val LsiMethod.isSuspend: Boolean
    get() = hasAnnotation("kotlin.coroutines.Suspend")

val LsiMethod.isComposable: Boolean
    get() = hasAnnotation("androidx.compose.runtime.Composable")

/**
 * 检查方法是否没有必需参数
 * @return 如果方法没有参数或所有参数都有默认值，则返回true，否则返回false
 */
val LsiMethod.hasNoRequiredParameters: Boolean
    get() = parameters.isEmpty() || parameters.all { it.hasDefault }

/**
 * 获取声明此方法的父类
 * @return 声明此方法的类，如果不存在则返回null
 */
val LsiMethod.parentClass: LsiClass?
    get() = declaringClass
