package site.addzero.easycode.inter

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import kotlin.reflect.KClass

data class MetadataContext<T>(
    val metadata: List<T>,
    val dependencies: List<KSFile>,
)

/**
 * 元数据提取器接口
 *
 * 负责从KSP符号中提取元数据，与代码生成逻辑解耦
 *
 * @param T 元数据类型
 */
interface AbsCodegenContext<T : Any> {
    val clazz: KClass<T>

    /**
     * 从解析器中提取元数据
     *
     * @param resolver KSP解析器
     * @return 提取的元数据列表
     */
    fun extract(resolver: Resolver): MetadataContext<T>


    /** 模板上下文 */
    val templateContext: List<TemplateContext<T>>
}

interface TemplateContext<T> {
    val codeGenerator: CodeGenerator?

    /**
    跳过存在的文件
     */
    val skipExistFile: Boolean

    val useKspCodeGenerator: Boolean


    val getRelativePath: String
    val getPkg: String
    val getFileName:(T,Map<String,String>)-> String
    val getFileSuffix: String

    /**
    模板绝对路径
     */
    val templatePath: String


}

