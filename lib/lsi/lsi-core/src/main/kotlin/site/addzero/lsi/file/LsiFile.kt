package site.addzero.lsi.file

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass

/**
 * 语言无关的文件结构抽象接口
 * Lsi = Language Structure Interface
 */
interface LsiFile {
    val name: String
    /**
     * 获取文件路径
     */
    val filePath: String?

    /**
     * 获取包名
     */
    val packageName: String?

    /**
     * 获取文件中定义的所有类
     */
    val classes: List<LsiClass>

    /**
     * 根据类名查找类
     */
    fun findClassByName(name: String): LsiClass?

    /**
     * 获取文件的注释
     */
    val comment: String?

    /**
     * 获取文件上的注解
     */
    val annotations: List<LsiAnnotation>

    /**
     * 当前打开的LsiClass
     * 通常指光标所在位置的类，如果无法确定则返回文件中的第一个类
     */
    val currentClass: LsiClass?
}
