package site.addzero.util.lsi_impl.impl.psi.psifile

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import site.addzero.util.lsi.file.isCurrentClassPojo
import site.addzero.util.lsi.file.LsiFile
import site.addzero.util.lsi_impl.impl.intellij.element.getCurrentPsiElement
import site.addzero.util.lsi_impl.impl.psi.clazz.PsiLsiClass
import site.addzero.util.lsi_impl.impl.psi.clazz.isPojo
import site.addzero.util.lsi_impl.impl.psi.file.PsiLsiFile

inline fun <reified T : PsiNameIdentifierOwner> PsiFile.convertToByChildren(): T? {
    return PsiTreeUtil.findChildOfType(originalElement, T::class.java)
}


fun PsiFile.getQualifiedClassName(): String? {
    val fileNameWithoutExtension = this.virtualFile.nameWithoutExtension
    val packageName = when (this) {
        is PsiJavaFile -> this.packageName
        else -> null
    }
    return if (packageName != null) {
        "$packageName.$fileNameWithoutExtension"
    } else {
        fileNameWithoutExtension
    }
}

fun PsiFile?.getPackagePath(): String? {
    val qualifiedClassName = this!!.getQualifiedClassName()
    return qualifiedClassName
}

fun PsiFile.toPsiClass(): PsiClass? {
    val childOfType = this.getChildOfType<PsiClass>()
    return childOfType
//    return  PsiTreeUtil.findChildOfType(this, PsiClass::class.java)
}

// ==================== POJO 检测 ====================

/**
 * 判断当前文件是否为Java POJO类
 * 检查当前编辑器光标所在位置的类是否为POJO
 *
 * 注意：此方法已重构为使用 LSI 抽象层
 *
 * @param editor 编辑器实例
 * @return true表示是Java POJO类，false表示不是
 */
fun isJavaPojo(editor: Editor?, file: PsiFile?): Boolean {
    if (file !is PsiJavaFile) return false

    val element = file.getCurrentPsiElement(editor) ?: return false
    val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return false

    // 方式1: 直接使用 PSI 检测
    return psiClass.isPojo()
    
    // 方式2: 使用 LSI 抽象层检测（推荐，更灵活）
    // val lsiFile = PsiLsiFile(file)
    // val currentClass = PsiLsiClass(psiClass)
    // return lsiFile.isCurrentClassPojo(currentClass)
}

/**
 * 判断当前文件是否为Java POJO类
 * 作为PsiFile的扩展函数使用
 *
 * @param editor 编辑器实例
 * @return true表示是Java POJO类，false表示不是
 */
fun PsiFile?.isJavaPojo(editor: Editor?): Boolean {
    return isJavaPojo(editor, this)
}

/**
 * 将 PsiFile 转换为 LsiFile 并检测是否为 POJO
 * 
 * 这是推荐的方式，使用 LSI 抽象层
 */
fun PsiFile?.toLsiFileAndCheckPojo(editor: Editor?): Boolean {
    val javaFile = this as? PsiJavaFile ?: return false
    val lsiFile: LsiFile = PsiLsiFile(javaFile)
    
    // 获取光标位置的类
    val element = this.getCurrentPsiElement(editor)
    val psiClass = element?.let { PsiTreeUtil.getParentOfType(it, PsiClass::class.java) }
    val currentClass = psiClass?.let { PsiLsiClass(it) }
    
    return lsiFile.isCurrentClassPojo(currentClass)
}
