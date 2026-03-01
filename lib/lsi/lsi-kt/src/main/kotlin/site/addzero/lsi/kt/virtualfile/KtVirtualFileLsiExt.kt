package site.addzero.lsi.kt.virtualfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.kt.clazz.KtLsiClass

/**
 * Kotlin实现：从VirtualFile提取LSI类
 * 注意：使用反射和类名匹配来避免类加载器冲突问题
 */

/**
 * 从Kotlin文件中提取所有类并转换为LSI类
 * 支持 class, interface, enum class 等声明
 */
fun VirtualFile.toAllKtLsiClasses(project: Project): List<LsiClass> {
    val psiFile = PsiManager.getInstance(project).findFile(this)
    if (psiFile == null) {
        println("[KT-DEBUG] ${this.name}: PsiManager.findFile 返回 null")
        return emptyList()
    }
    
    val className = psiFile::class.java.name
    if (className != "org.jetbrains.kotlin.psi.KtFile" && !className.endsWith(".KtFile")) {
        println("[KT-DEBUG] ${this.name}: 不是 KtFile，实际类型: $className")
        return emptyList()
    }
    
    // 使用反射获取 declarations，避免类加载器问题
    val declarations = try {
        val method = psiFile::class.java.getMethod("getDeclarations")
        @Suppress("UNCHECKED_CAST")
        method.invoke(psiFile) as? List<PsiElement> ?: emptyList()
    } catch (e: Exception) {
        println("[KT-DEBUG] ${this.name}: 反射获取 declarations 失败: ${e.message}")
        emptyList()
    }
    
    // 使用类名匹配过滤 KtClass（包括 interface、enum class 等）
    val ktClasses = declarations.filter { decl ->
        val declClassName = decl::class.java.name
        declClassName == "org.jetbrains.kotlin.psi.KtClass" || declClassName.endsWith(".KtClass")
    }
    
    println("[KT-DEBUG] ${this.name}: declarations=${declarations.size}, ktClasses=${ktClasses.size}, types=${declarations.map { it::class.java.simpleName }}")
    
    // 使用反射构造 KtLsiClass，避免类加载器问题
    return ktClasses.map { decl ->
        KtLsiClass(decl, true)
    }
}

/**
 * 从Kotlin文件中提取所有KtClass
 */
fun VirtualFile.toAllKtClasses(project: Project): List<KtClass> {
    val psiFile = PsiManager.getInstance(project).findFile(this) ?: return emptyList()
    
    val className = psiFile::class.java.name
    if (className != "org.jetbrains.kotlin.psi.KtFile" && !className.endsWith(".KtFile")) {
        return emptyList()
    }
    
    val declarations = try {
        val method = psiFile::class.java.getMethod("getDeclarations")
        @Suppress("UNCHECKED_CAST")
        method.invoke(psiFile) as? List<PsiElement> ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    return declarations.filter { decl ->
        val declClassName = decl::class.java.name
        declClassName == "org.jetbrains.kotlin.psi.KtClass" || declClassName.endsWith(".KtClass")
    }.mapNotNull { 
        @Suppress("UNCHECKED_CAST")
        it as? KtClass 
    }
}
