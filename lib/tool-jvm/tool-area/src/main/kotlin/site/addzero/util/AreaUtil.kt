package site.addzero.util


interface AreaUtil<T> {
    fun compare(version1: String, version2: String): Int
    fun getChildren(t: T): List<T>
    fun setChildren(t: T): List<T>
}
