//package site.addzero.util.lsi.type.spec
//
//
//interface LsiPrimitiveType {
//    val byte: String
//    val short: String
//    val int: String
//    val long: String
//    val float: String
//    val double: String
//    val boolean: String
//    val char: String
//}
//
//interface JavaLsiPrimitiveType : LsiPrimitiveType {
//    override val byte: String
//        get() = "java.lang.Byte"
//    override val short: String
//        get() = "java.lang.Short"
//    override val int: String
//        get() = "java.lang.Integer"
//    override val long: String
//        get() = "java.lang.Long"
//    override val float: String
//        get() = "java.lang.Float"
//    override val double: String
//        get() = "java.lang.Double"
//    override val boolean: String
//        get() = "java.lang.Boolean"
//    override val char: String
//        get() = "java.lang.Character"
//}
//
//interface KotlinLsiPrimitiveType : LsiPrimitiveType {
//    override val byte: String
//        get() = Byte::class.qualifiedName!!
//    override val short: String
//        get() = Short::class.qualifiedName!!
//    override val int: String
//        get() = Int::class.qualifiedName!!
//    override val long: String
//        get() = Long::class.qualifiedName!!
//    override val float: String
//        get() = Float::class.qualifiedName!!
//    override val double: String
//        get() = Double::class.qualifiedName!!
//    override val boolean: String
//        get() = Boolean::class.qualifiedName!!
//    override val char: String
//        get() = Char::class.qualifiedName!!
//}
