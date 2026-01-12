package site.addzero.util.lsi_impl.impl.kt.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.file.LsiFile
import site.addzero.util.lsi.project.LsiProject
import site.addzero.util.lsi_impl.impl.kt.clazz.KtLsiClass
import site.addzero.util.lsi_impl.impl.kt.file.KtLsiFile


fun Project.toLsiKtProject(): LsiProject {
     val project = this
     return    object : LsiProject {
        override val name: String?
            get() = project.name
        override val basePath: String?
            get() = project.basePath
        override val files: List<LsiFile>
            get() {
                val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                return kotlinFiles.mapNotNull { virtualFile ->
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile is KtFile) {
                        KtLsiFile(psiFile)
                    } else {
                        null
                    }
                }
            }
        override fun findFileByPath(path: String): LsiFile? {
            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path") ?: return null
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null
            return if (psiFile is KtFile) KtLsiFile(psiFile) else null
        }

        override fun findClassByName(name: String): LsiClass? {
            val psiFacade = JavaPsiFacade.getInstance(project)
            val scope = GlobalSearchScope.projectScope(project)

            // 先尝试直接查找完整类名
            val psiClass = psiFacade.findClass(name, scope)

            // 如果找到了类，并且是 Kotlin Light Class，则获取对应的 KtClass
            if (psiClass is KtLightClass) {
                return KtLsiClass(psiClass.kotlinOrigin as KtClass)
            }

            // 如果没找到，尝试在项目中搜索
            val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            for (virtualFile in kotlinFiles) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile is KtFile) {
                    val ktClass = psiFile.declarations.filterIsInstance<KtClass>().find { it.name == name }
                    if (ktClass != null) {
                        return KtLsiClass(ktClass)
                    }
                }
            }

            return null
        }

        override fun findClassesByAnnotation(annotationName: String): List<LsiClass> {
            val result = mutableListOf<LsiClass>()
            val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))

            for (virtualFile in kotlinFiles) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile is KtFile) {
                    val ktClasses = psiFile.declarations.filterIsInstance<KtClass>()
                    for (ktClass in ktClasses) {
                        val hasAnnotation = ktClass.annotationEntries.any { annotation ->
                            val shortName = annotation.shortName?.asString()
                            val fqName = annotation.toLightAnnotation()?.qualifiedName
                            shortName == annotationName || fqName?.endsWith(annotationName) == true
                        }
                        if (hasAnnotation) {
                            result.add(KtLsiClass(ktClass))
                        }
                    }
                }
            }

            return result
        }

        override val classes: List<LsiClass>
            get() {
                val result = mutableListOf<LsiClass>()
                val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))

                for (virtualFile in kotlinFiles) {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile is KtFile) {
                        val ktClasses = psiFile.declarations.filterIsInstance<KtClass>()
                        for (ktClass in ktClasses) {
                            result.add(KtLsiClass(ktClass))
                        }
                    }
                }

                return result
            }
        override val isJavaProject: Boolean
            get() = false
        override val isKotlinProject: Boolean
            get() = true
    }

}
