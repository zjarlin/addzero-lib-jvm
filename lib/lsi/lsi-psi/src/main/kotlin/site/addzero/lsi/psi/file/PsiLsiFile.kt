package site.addzero.lsi.psi.file

import com.intellij.psi.PsiJavaFile
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.psi.anno.PsiLsiAnnotation
import site.addzero.lsi.psi.clazz.PsiLsiClass

/**
 * 基于 PSI 的 LsiFile 实现 (Java files)
 */
class PsiLsiFile(private val psiJavaFile: PsiJavaFile) : LsiFile {
    override val name: String
        get() = psiJavaFile.name
    override val filePath: String?
        get() = psiJavaFile.virtualFile?.path

    override val packageName: String?
        get() = psiJavaFile.packageName

    override val classes: List<LsiClass>
        get() = psiJavaFile.classes.map { PsiLsiClass(it) }

    override fun findClassByName(name: String): LsiClass? {
        val psiClass = psiJavaFile.classes.find { it.name == name }
        return psiClass?.let { PsiLsiClass(it) }
    }

    override val comment: String?
        get() {
            return psiJavaFile.classes.firstOrNull()?.docComment?.text
        }

    override val annotations: List<LsiAnnotation>
        get() {
            // Java files don't have file-level annotations
            // Return empty list or package annotations if needed
            return psiJavaFile.packageStatement?.annotationList?.annotations?.map {
                PsiLsiAnnotation(it)
            } ?: emptyList()
        }

    override val currentClass: LsiClass?
        get() {
            // 返回文件中的第一个类
            // 在更高级的实现中，可以根据光标位置确定当前类
            val firstClass = psiJavaFile.classes.firstOrNull()
            return firstClass?.let { PsiLsiClass(it) }
        }
}
