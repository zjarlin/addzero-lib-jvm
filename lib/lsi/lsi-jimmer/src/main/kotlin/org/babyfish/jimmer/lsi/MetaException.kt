//package org.babyfish.jimmer.lsi
//
//import site.addzero.util.lsi.clazz.LsiClass
//
//open class MetaException(
//    val declaration: LsiClass,
//    childDeclaration: LsiClass?,
//    reason: String,
//    cause: Throwable? = null
//) : RuntimeException(
//    message(
//        declaration,
//        if (childDeclaration === null || childDeclaration === declaration) {
//            reason
//        } else {
//            message(childDeclaration, reason)
//        }
//    ),
//    cause
//) {
//
//    constructor(
//        declaration: LsiClass,
//        reason: String,
//        cause: Throwable? = null
//    ) : this(
//        declaration, null, reason, cause
//    )
//
//    companion object {
//
//        @JvmStatic
//        private fun message(declaration: LsiClass, reason: String): String =
//            """
//                   Illegal  ${declaration.qualifiedName} $reason
//                """.trimIndent()
//
//
//    }
//}
