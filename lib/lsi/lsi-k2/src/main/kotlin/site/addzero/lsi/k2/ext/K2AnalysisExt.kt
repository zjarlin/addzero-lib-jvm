package site.addzero.lsi.k2.ext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.k2.clazz.K2LsiClass
import site.addzero.lsi.k2.field.K2LsiField

/**
 * K2 Analysis API 扩展函数
 * 提供便捷的入口点将 Kotlin PSI 元素转换为 LSI 抽象
 */

/**
 * 将 KtClass 转换为 LsiClass（使用 K2 Analysis API）
 */
fun KtClass.toLsiClassK2(): LsiClass {
    return analyze(this) {
        val symbol = this@toLsiClassK2.symbol
        if (symbol is KaClassSymbol) {
            K2LsiClass(this@toLsiClassK2, symbol, this)
        } else {
            throw IllegalStateException("Expected KaClassSymbol but got ${symbol::class.simpleName}")
        }
    }
}

/**
 * 将 KtProperty 转换为 LsiField（使用 K2 Analysis API）
 */
fun KtProperty.toLsiFieldK2(): LsiField {
    return analyze(this) {
        val symbol = this@toLsiFieldK2.symbol
        if (symbol is KaPropertySymbol) {
            K2LsiField(this@toLsiFieldK2, symbol, this)
        } else {
            throw IllegalStateException("Expected KaPropertySymbol but got ${symbol::class.simpleName}")
        }
    }
}

/**
 * 在 analyze 块中执行操作，提供 KaSession 上下文
 */
inline fun <T> KtClass.withK2Analysis(crossinline block: KaSession.(K2LsiClass) -> T): T {
    return analyze(this) {
        val symbol = this@withK2Analysis.symbol
        if (symbol is KaClassSymbol) {
            val lsiClass = K2LsiClass(this@withK2Analysis, symbol, this)
            block(lsiClass)
        } else {
            throw IllegalStateException("Expected KaClassSymbol but got ${symbol::class.simpleName}")
        }
    }
}

/**
 * 在 analyze 块中执行操作，提供 KaSession 上下文
 */
inline fun <T> KtProperty.withK2Analysis(crossinline block: KaSession.(K2LsiField) -> T): T {
    return analyze(this) {
        val symbol = this@withK2Analysis.symbol
        if (symbol is KaPropertySymbol) {
            val lsiField = K2LsiField(this@withK2Analysis, symbol, this)
            block(lsiField)
        } else {
            throw IllegalStateException("Expected KaPropertySymbol but got ${symbol::class.simpleName}")
        }
    }
}

/**
 * 批量转换 KtClass 列表为 LsiClass 列表（共享同一个 analyze 会话以提高性能）
 */
fun List<KtClass>.toLsiClassesK2(): List<LsiClass> {
    if (isEmpty()) return emptyList()
    return analyze(first()) {
        this@toLsiClassesK2.mapNotNull { ktClass ->
            val symbol = ktClass.symbol
            if (symbol is KaClassSymbol) {
                K2LsiClass(ktClass, symbol, this)
            } else null
        }
    }
}

/**
 * 检查 KtClass 是否为 POJO（使用 K2 Analysis API）
 */
fun KtClass.isPojoK2(): Boolean {
    return toLsiClassK2().isPojo
}

/**
 * 获取 KtClass 的全限定名（使用 K2 Analysis API）
 */
fun KtClass.qualifiedNameK2(): String? {
    return analyze(this) {
        val symbol = this@qualifiedNameK2.symbol
        if (symbol is KaClassSymbol) {
            symbol.classId?.asFqNameString()
        } else null
    }
}

/**
 * 获取 KtProperty 的类型全限定名（使用 K2 Analysis API）
 */
fun KtProperty.typeQualifiedNameK2(): String? {
    return analyze(this) {
        val symbol = this@typeQualifiedNameK2.symbol
        if (symbol is KaPropertySymbol) {
            val returnType = symbol.returnType
            if (returnType is KaClassType) {
                returnType.classId.asFqNameString()
            } else null
        } else null
    }
}
