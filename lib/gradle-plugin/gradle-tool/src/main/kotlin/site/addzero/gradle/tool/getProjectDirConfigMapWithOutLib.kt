
// 定义黑名单目录列表
val BLACKLIST_DIRS = listOf("buildSrc,", "build-logic")
fun getProjectDirConfigMapWithOutLib(rootDir: File): MutableMap<String, String> {
    val projectDirConfigMapResult = getProjectDirConfigMap(BLACKLIST_DIRS + listOf("lib"), rootDir)
    return projectDirConfigMapResult
}
fun getProjectDirConfigMap(blackDir: List<String> = BLACKLIST_DIRS, rootDir: File): MutableMap<String, String> {
    val projectDirConfigMapResult = generateProjectDirConfigMap(blackDir, rootDir)
    val mutableMapOf = mutableMapOf<String, String>()
    projectDirConfigMapResult.forEach { modelConfig ->
        val moduleName = modelConfig.moduleName
        mutableMapOf["${moduleName}SourceDir"] = modelConfig.sourceDir
        mutableMapOf["${moduleName}BuildDir"] = modelConfig.buildDir
        modelConfig.resourceDir?.let {
            mutableMapOf["${moduleName}ResourceDir"] = it
        }
    }
    return mutableMapOf
}
