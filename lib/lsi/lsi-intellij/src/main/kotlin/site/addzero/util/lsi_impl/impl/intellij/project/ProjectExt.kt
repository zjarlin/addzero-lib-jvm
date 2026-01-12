package site.addzero.util.lsi_impl.impl.intellij.project

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass


fun Project.toVirtualFile(): VirtualFile? {
    val instance = FileEditorManager.getInstance(this)
    val file = instance.selectedEditor?.file
    return file
}

fun Project.toEditor(): Editor? {
    val instance = FileEditorManager.getInstance(this)
    return instance.selectedTextEditor
}

/**
 * 查找 KtClass by 名称
 */
fun Project.findKtClassByName(className: String): KtClass? {
    // 使用 JavaPsiFacade 查找类
    val psiFacade = JavaPsiFacade.getInstance(this)
    val scope = GlobalSearchScope.projectScope(this)

    // 先尝试直接查找完整类名
    val psiClass = psiFacade.findClass(className, scope)

    // 如果找到了类，并且是 Kotlin Light Class，则获取对应的 KtClass
    if (psiClass is KtLightClass) {
        return psiClass.kotlinOrigin as? KtClass
    }

    // 如果没有找到，尝试在不同的包中查找
    val shortName = className.substringAfterLast('.')
    val foundClasses = psiFacade.findClasses(shortName, scope)

    return foundClasses
        .filterIsInstance<KtLightClass>()
        .firstOrNull { it.qualifiedName == className }
        ?.kotlinOrigin as? KtClass
}

/**项目正在索引中  */
val Project.isDumb: Boolean
    get() {
        return DumbService.getInstance(this).isDumb
    }


fun Project.isKotlinProject(): Boolean {
    // 检查是否存在 Kotlin 文件
    val hasKotlinFiles = FileTypeIndex.containsFileOfType(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(this))
    // 检查是否存在 Java 文件
    val hasJavaFiles = FileTypeIndex.containsFileOfType(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(this))

    return when {
        hasKotlinFiles && !hasJavaFiles -> true  // 纯 Kotlin 项目
        !hasKotlinFiles && hasJavaFiles -> false // 纯 Java 项目
        hasKotlinFiles && hasJavaFiles -> true   // 混合项目，但包含 Kotlin
        else -> false                            // 默认返回 false
    }
}


fun Project.isJavaProject(): Boolean {
    return !isKotlinProject() // 如果没有检测到 Kotlin，就认为是 Java 项目
}
