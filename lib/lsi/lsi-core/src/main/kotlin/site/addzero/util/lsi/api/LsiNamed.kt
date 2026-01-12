package site.addzero.util.lsi.api

/**
 * 暂时等LSI对齐完善后抽接口逻辑出来
 */
interface LsiNamed {
    val simpleName: String get() {
        return qualifiedName.substringAfterLast('.')
    }
    val qualifiedName: String
}