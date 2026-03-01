package site.addzero.lsi.psi.method

import site.addzero.lsi.psi.anno.guessFieldCommentOrNull
import com.intellij.psi.PsiMethod
import site.addzero.util.str.cleanDocComment

fun PsiMethod.getComment(): String {
    // 尝试从注解中获取描述
    val annotations = this.annotations
    val guessFieldCommentOrNull = annotations.guessFieldCommentOrNull()
    return guessFieldCommentOrNull?: cleanDocComment(this.docComment?.text)
}
