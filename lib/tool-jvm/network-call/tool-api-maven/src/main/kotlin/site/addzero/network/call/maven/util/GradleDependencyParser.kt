package site.addzero.network.call.maven.util

/**
 * Gradle 依赖字符串解析器
 * 
 * 支持解析以下格式：
 * - implementation("group:artifact:version")
 * - implementation('group:artifact:version')
 * - implementation "group:artifact:version"
 * - testImplementation("group:artifact:version")
 * - api("group:artifact:version")
 * 等等
 */
object GradleDependencyParser {

    /**
     * 依赖坐标数据类
     */
    data class DependencyCoordinate(
        val groupId: String,
        val artifactId: String,
        val version: String,
        val configuration: String = "implementation" // implementation, api, testImplementation 等
    ) {
        /**
         * 转换为标准的 Maven 坐标格式
         */
        fun toMavenCoordinate(): String = "$groupId:$artifactId:$version"
        
        /**
         * 转换为 Gradle 依赖声明
         */
        fun toGradleDependency(useDoubleQuote: Boolean = true): String {
            val quote = if (useDoubleQuote) "\"" else "'"
            return "$configuration($quote$groupId:$artifactId:$version$quote)"
        }
    }

    /**
     * 从 Gradle 依赖字符串中解析依赖坐标
     * 
     * @param dependencyString Gradle 依赖字符串，如: implementation("com.google.inject:guice:4.2.3")
     * @return 解析后的依赖坐标，如果解析失败返回 null
     */
    fun parseDependency(dependencyString: String): DependencyCoordinate? {
        // 移除首尾空格
        val trimmed = dependencyString.trim()
        
        // 正则表达式匹配 Gradle 依赖声明
        // 支持: implementation("group:artifact:version") 或 implementation('group:artifact:version')
        // 或 implementation "group:artifact:version"
        val pattern = Regex(
            """(\w+)\s*\(?\s*["']([^:"']+):([^:"']+):([^:"']+)["']\s*\)?"""
        )
        
        val matchResult = pattern.find(trimmed) ?: return null
        
        val (configuration, groupId, artifactId, version) = matchResult.destructured
        
        return DependencyCoordinate(
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            configuration = configuration
        )
    }

    /**
     * 更新依赖字符串到最新版本
     * 
     * @param dependencyString 原始 Gradle 依赖字符串
     * @return 更新后的依赖字符串，如果无法获取最新版本则返回原字符串
     */
    fun updateToLatestVersion(dependencyString: String): String {
        val coordinate = parseDependency(dependencyString) ?: return dependencyString
        
        // 获取最新版本
        val latestVersion = MavenCentralSearchUtil.getLatestVersion(
            coordinate.groupId,
            coordinate.artifactId
        ) ?: return dependencyString
        
        // 检测原字符串使用的引号类型
        val useDoubleQuote = dependencyString.contains('"')
        
        // 生成新的依赖字符串
        val updatedCoordinate = coordinate.copy(version = latestVersion)
        return updatedCoordinate.toGradleDependency(useDoubleQuote)
    }

    /**
     * 批量更新依赖字符串到最新版本
     * 
     * @param dependencyStrings 依赖字符串列表
     * @return 更新结果列表，包含原字符串、新字符串和更新状态
     */
    fun batchUpdateToLatestVersion(dependencyStrings: List<String>): List<UpdateResult> {
        return dependencyStrings.map { original ->
            val updated = updateToLatestVersion(original)
            UpdateResult(
                original = original,
                updated = updated,
                isUpdated = original != updated,
                coordinate = parseDependency(original)
            )
        }
    }

    /**
     * 更新结果数据类
     */
    data class UpdateResult(
        val original: String,
        val updated: String,
        val isUpdated: Boolean,
        val coordinate: DependencyCoordinate?
    ) {
        val oldVersion: String?
            get() = coordinate?.version
        
        val newVersion: String?
            get() = if (isUpdated) {
                parseDependency(updated)?.version
            } else {
                null
            }
    }

    /**
     * 从依赖字符串中提取 Maven 坐标
     * 
     * @param dependencyString Gradle 依赖字符串
     * @return Maven 坐标字符串 (groupId:artifactId:version)，如果解析失败返回 null
     */
    fun extractMavenCoordinate(dependencyString: String): String? {
        return parseDependency(dependencyString)?.toMavenCoordinate()
    }

    /**
     * 验证依赖字符串格式是否正确
     * 
     * @param dependencyString 依赖字符串
     * @return true 如果格式正确，false 如果格式错误
     */
    fun isValidDependencyString(dependencyString: String): Boolean {
        return parseDependency(dependencyString) != null
    }
}
