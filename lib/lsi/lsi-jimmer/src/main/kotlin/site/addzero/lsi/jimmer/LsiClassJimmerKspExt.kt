package site.addzero.lsi.jimmer

import com.squareup.kotlinpoet.ClassName
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.ksp.clazz.toClassName
import site.addzero.lsi.ksp.clazz.toNestedClassName

private const val DRAFT = "Draft"
private const val PROPS = "Props"
private const val FETCHER_DSL = "FetcherDsl"

/** 实体接口本身的 ClassName，例如 `com.example.Book` */
val LsiClass.className: ClassName
    get() = toClassName()

/** Props 对象的 ClassName，例如 `com.example.BookProps` */
val LsiClass.propsClassName: ClassName
    get() = toClassName { "$it$PROPS" }

/** Draft 接口的 ClassName，例如 `com.example.BookDraft` */
val LsiClass.draftClassName: ClassName
    get() = toClassName { "$it$DRAFT" }

/** FetcherDsl 接口的 ClassName，例如 `com.example.BookFetcherDsl` */
val LsiClass.fetcherDslClassName: ClassName
    get() = toClassName { "$it$FETCHER_DSL" }

/**
 * 带嵌套类路径的 Draft ClassName，例如 `BookDraft.Producer.Builder`。
 *
 * @param nestedNames 嵌套类名列表，追加在 "BookDraft" 之后。
 */
fun LsiClass.draftClassName(vararg nestedNames: String): ClassName =
    toNestedClassName { simpleName ->
        mutableListOf<String>().apply {
            add("$simpleName$DRAFT")
            addAll(nestedNames)
        }
    }
