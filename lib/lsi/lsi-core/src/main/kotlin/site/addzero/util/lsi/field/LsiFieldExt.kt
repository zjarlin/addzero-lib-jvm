package site.addzero.util.lsi.field


/**
 * 获取字段注解的指定参数值
 * @param annotationSimpleName 注解全限定名
 * @param parameterName 参数名称
 * @return 参数值，如果不存在则返回 null
 */
fun LsiField.getArg(annotationSimpleName: String, parameterName: String = "value"): String? {
    val annotation = annotations.find { it.simpleName == annotationSimpleName } ?: return null
    val toString1 = annotation.getAttribute(parameterName)?.toString()
    val toString = toString1
    return toString
}

/**
 * 检查字段是否具有指定的注解
 * @param annotationNames 注解全限定名数组
 * @return 如果字段具有其中任何一个注解，则返回true，否则返回false
 */
fun LsiField.hasAnnotation(vararg annotationNames: String): Boolean {
    return annotationNames.any { annotationName ->
        annotations.any { annotation ->
            annotation.simpleName == annotationName
        }
    }
}

fun LsiField.hasAnnotationIgnoreCase(vararg annotationNames: String): Boolean {
    return annotationNames.any { annotationName ->
        annotations.any { annotation ->
            annotation.simpleName.equals(annotationName, true)
        }
    }
}

/**
 * 检查字段是否具有指定的注解
 * @param annotationNames 注解全限定名数组
 * @return 如果字段具有其中任何一个注解，则返回true，否则返回false
 */
fun LsiField.hasFqAnnotation(vararg annotationNames: String): Boolean {
    return annotationNames.any { annotationName ->
        annotations.any { annotation ->
            annotation.qualifiedName == annotationName
        }
    }
}


//val LsiField.isNullable: Boolean
//    get() {
//        val isPrimitive = this.type?.isPrimitive
//        val nullable = annotations.isNullable(isPrimitive == true)
//        return nullable
//    }

