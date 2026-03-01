package site.addzero.lsi.file

import site.addzero.lsi.clazz.LsiClass

/**
 * LsiFile POJO 检测器
 *
 * 提供基于 LSI 抽象的 POJO 检测功能，可在任何 LSI 实现中复用
 */

/**
 * 检测文件中光标位置的类是否为 POJO
 *
 * 此方法需要配合具体实现（PSI, Kotlin PSI）提供的光标位置信息
 *
 * @param currentClass 当前光标所在的类
 * @return true 表示是 POJO 类，false 表示不是
 */
fun LsiFile.isCurrentClassPojo(currentClass: LsiClass? = this.currentClass): Boolean {
    val clazz = currentClass ?: return false
    return clazz.isPojo
}

/**
 * 检测文件是否为 POJO 文件
 *
 * 定义：文件中至少有一个 POJO 类
 *
 * @return true 表示是 POJO 文件，false 表示不是
 */
fun LsiFile.isPojoFile(): Boolean {
    return classes.any { it.isPojo }
}

/**
 * 检测文件是否为纯 POJO 文件
 *
 * 定义：文件中所有非接口非枚举的类都是 POJO
 *
 * @return true 表示是纯 POJO 文件，false 表示不是
 */
fun LsiFile.isPurePojoFile(): Boolean {
    val regularClasses = classes.filter { !it.isInterface && !it.isEnum }
    if (regularClasses.isEmpty()) return false
    return regularClasses.all { it.isPojo }
}

/**
 * 检测文件主类是否为 POJO
 *
 * @return true 表示主类是 POJO，false 表示不是
 */
fun LsiFile.isMainClassPojo(): Boolean {
    val main = mainClass ?: return false
    return main.isPojo
}

/**
 * 获取文件中的第一个 POJO 类
 *
 * @return 第一个 POJO 类，如果没有则返回 null
 */
fun LsiFile.findFirstPojoClass(): LsiClass? {
    return classes.firstOrNull { it.isPojo }
}

/**
 * 根据名称查找 POJO 类
 *
 * @param name 类名
 * @return POJO 类，如果没有或不是 POJO 则返回 null
 */
fun LsiFile.findPojoClassByName(name: String): LsiClass? {
    val clazz = findClassByName(name) ?: return null
    return if (clazz.isPojo) clazz else null
}
