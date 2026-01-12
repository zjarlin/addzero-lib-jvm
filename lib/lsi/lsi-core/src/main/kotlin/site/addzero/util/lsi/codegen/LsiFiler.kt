package site.addzero.util.lsi.codegen

interface LsiFiler {
    /**
     * 创建源文件
     */
    fun createSourceFile(qualifiedName: String, content: String)

}
