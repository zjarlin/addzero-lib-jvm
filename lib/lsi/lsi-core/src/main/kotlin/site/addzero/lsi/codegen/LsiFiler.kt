package site.addzero.lsi.codegen

interface LsiFiler {
    /**
     * 创建源文件
     */
    fun createSourceFile(qualifiedName: String, content: String)

}
