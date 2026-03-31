package site.addzero.lsi.ksp.file

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.ksp.anno.KspLsiAnnotation
import site.addzero.lsi.ksp.clazz.KspLsiClass
import site.addzero.lsi.ksp.clazz.toLsiClass

class KspLsiFile(internal val resolver: Resolver, private val kspFile: KSFile) : LsiFile {
  override val name: String
    get() = kspFile.fileName
  override val filePath: String?
    get() = kspFile.filePath
  override val packageName: String?
    get() = kspFile.packageName.asString()

  override val classes: List<LsiClass> by lazy {
    kspFile.getAllClassDeclarations().map { KspLsiClass(resolver, it) }.toList()
  }

  override fun findClassByName(name: String): LsiClass? {
    val firstOrNull = kspFile.getAllClassDeclarations().firstOrNull { it.simpleName.asString() == name }
    val toLsiClass = firstOrNull?.toLsiClass(resolver)
    return toLsiClass
  }

  override val comment: String? by lazy {
    kspFile.getAllClassDeclarations().firstOrNull()?.docString
  }
  override val annotations: List<LsiAnnotation>
    get() = kspFile.annotations.map { KspLsiAnnotation(it) }.toList()
  override val currentClass: LsiClass?
    get() {
      val firstOrNull = kspFile.getAllClassDeclarations().firstOrNull()
      val toLsiClass = firstOrNull?.toLsiClass(resolver)
      return toLsiClass
    }
}
