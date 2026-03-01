package site.addzero.lsi.kt.file

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.file.isCurrentClassPojo
import site.addzero.lsi.kt.clazz.KtLsiClass
import site.addzero.lsi.kt.clazz.isPojo
import site.addzero.lsi.intellij.element.getCurrentPsiElement

// ==================== POJO 检测 ====================

/**
 * 判断当前文件是否为Kotlin POJO类
 * 检查当前编辑器光标所在位置的类是否为POJO
 *
 * 注意：此方法已重构为使用 LSI 抽象层
 *
 * @param editor 编辑器实例
 * @return true表示是Kotlin POJO类，false表示不是
 */
fun isKotlinPojo(editor: Editor?, file: PsiFile?): Boolean {
    if (file !is KtFile) return false
    val element = file.getCurrentPsiElement(editor) ?: return false
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClass::class.java) ?: return false

    // 方式1: 直接使用 Kotlin PSI 检测
    return ktClass.isPojo()

    // 方式2: 使用 LSI 抽象层检测（推荐，更灵活）
    // val lsiFile = KtLsiFile(file)
    // val currentClass = KtLsiClass(ktClass)
    // return lsiFile.isCurrentClassPojo(currentClass)
}

/**
 * 判断当前文件是否为Kotlin POJO类
 * 作为PsiFile的扩展函数使用
 *
 * @param editor 编辑器实例
 * @return true表示是Kotlin POJO类，false表示不是
 */
fun PsiFile?.isKotlinPojo(editor: Editor?): Boolean {
    return isKotlinPojo(editor, this)
}

/**
 * 将 KtFile 转换为 LsiFile 并检测是否为 POJO
 *
 * 这是推荐的方式，使用 LSI 抽象层
 */
fun PsiFile?.toLsiFileAndCheckPojo(editor: Editor?): Boolean {
    val ktFile = this as? KtFile ?: return false
    val lsiFile: LsiFile = KtLsiFile(ktFile)

    // 获取光标位置的类
    val element = this.getCurrentPsiElement(editor)
    val ktClass = element?.let { PsiTreeUtil.getParentOfType(it, KtClass::class.java) }
    val currentClass = ktClass?.let { KtLsiClass(it) }

    return lsiFile.isCurrentClassPojo(currentClass)
}
