package site.addzero.lsi.kt.file

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.kt.anno.KtLsiAnnotation
import site.addzero.lsi.kt.clazz.KtLsiClass

/**
 * 基于 Kotlin PSI 的 LsiFile 实现
 */
class KtLsiFile(private val ktFile: KtFile) : LsiFile {
    override val name: String
        get() = ktFile.name

    override val filePath: String?
        get() = ktFile.virtualFile?.path

    override val packageName: String?
        get() = ktFile.packageFqName.asString()

    override val classes: List<LsiClass>
        get() = ktFile.declarations
            .filterIsInstance<KtClass>()
            .map { KtLsiClass(it) }

    override fun findClassByName(name: String): LsiClass? {
        val ktClass = ktFile.declarations
            .filterIsInstance<KtClass>()
            .find { it.name == name }
        return ktClass?.let { KtLsiClass(it) }
    }

    override val comment: String?
        get() {
            return ktFile.getChildOfType<org.jetbrains.kotlin.kdoc.psi.api.KDoc>()?.text
        }

    override val annotations: List<LsiAnnotation>
        get() = ktFile.annotationEntries.map { KtLsiAnnotation(it) }

    override val currentClass: LsiClass?
        get() {
            // 返回文件中的第一个类
            // 在更高级的实现中，可以根据光标位置确定当前类
            val firstClass = ktFile.declarations.filterIsInstance<KtClass>().firstOrNull()
            return firstClass?.let { KtLsiClass(it) }
        }
}
