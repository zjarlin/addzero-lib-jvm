package site.addzero.lsi.psi.project

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinFileType
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.project.LsiProject
import site.addzero.lsi.psi.clazz.PsiLsiClass
import site.addzero.lsi.psi.file.PsiLsiFile

/**
 * 将 Project 转换为 Java 项目的 LsiProject 实现
 */
fun Project.toLsiJavaProject(): LsiProject {
    val project = this
    return object : LsiProject {
        override val name: String?
            get() = project.name
        
        override val basePath: String?
            get() = project.basePath
        
        override val files: List<LsiFile>
            get() {
                val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                return javaFiles.mapNotNull { virtualFile ->
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile is PsiJavaFile) {
                        PsiLsiFile(psiFile)
                    } else {
                        null
                    }
                }
            }
        
        override fun findFileByPath(path: String): LsiFile? {
            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path") ?: return null
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null
            return if (psiFile is PsiJavaFile) PsiLsiFile(psiFile) else null
        }
        
        override fun findClassByName(name: String): LsiClass? {
            val psiFacade = JavaPsiFacade.getInstance(project)
            val scope = GlobalSearchScope.projectScope(project)
            
            // 先尝试直接查找完整类名
            val psiClass = psiFacade.findClass(name, scope)
            if (psiClass != null) {
                return PsiLsiClass(psiClass)
            }
            
            // 如果没找到，尝试在项目中搜索
            val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            for (virtualFile in javaFiles) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile is PsiJavaFile) {
                    val foundClass = psiFile.classes.find { it.name == name }
                    if (foundClass != null) {
                        return PsiLsiClass(foundClass)
                    }
                }
            }
            
            return null
        }
        
        override fun findClassesByAnnotation(annotationName: String): List<LsiClass> {
            val result = mutableListOf<LsiClass>()
            val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            
            for (virtualFile in javaFiles) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile is PsiJavaFile) {
                    val classes = psiFile.classes
                    for (psiClass in classes) {
                        val hasAnnotation = psiClass.annotations.any { annotation ->
                            val shortName = annotation.nameReferenceElement?.referenceName
                            val qualifiedName = annotation.qualifiedName
                            shortName == annotationName || qualifiedName?.endsWith(annotationName) == true
                        }
                        if (hasAnnotation) {
                            result.add(PsiLsiClass(psiClass))
                        }
                    }
                }
            }
            
            return result
        }
        
        override val classes: List<LsiClass>
            get() {
                val result = mutableListOf<LsiClass>()
                val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                
                for (virtualFile in javaFiles) {
                    val instance = PsiManager.getInstance(project)
                    val psiFile = instance.findFile(virtualFile)
                    if (psiFile is PsiJavaFile) {
                        val psiClasses = psiFile.classes
                        for (psiClass in psiClasses) {
                            result.add(PsiLsiClass(psiClass))
                        }
                    }
                }
                
                return result
            }
        
        override val isJavaProject: Boolean
            get() = true
        
        override val isKotlinProject: Boolean
            get() = false
    }
}
