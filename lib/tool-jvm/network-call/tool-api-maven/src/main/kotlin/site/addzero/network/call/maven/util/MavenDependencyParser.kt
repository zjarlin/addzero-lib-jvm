package site.addzero.network.call.maven.util

/**
 * Maven 依赖标签解析器
 * 
 * 支持解析和更新 Maven pom.xml 中的依赖标签：
 * ```xml
 * <dependency>
 *     <groupId>com.google.inject</groupId>
 *     <artifactId>guice</artifactId>
 *     <version>4.2.3</version>
 * </dependency>
 * ```
 */
object MavenDependencyParser {

    /**
     * Maven 依赖坐标数据类
     */
    data class MavenDependencyCoordinate(
        val groupId: String,
        val artifactId: String,
        val version: String,
        val scope: String? = null,        // compile, test, provided, runtime, system
        val classifier: String? = null,   // sources, javadoc 等
        val type: String? = null,         // jar, war, pom 等
        val optional: Boolean? = null     // true, false
    ) {
        /**
         * 转换为标准的 Maven 坐标格式
         */
        fun toMavenCoordinate(): String = "$groupId:$artifactId:$version"
        
        /**
         * 转换为 Maven XML 格式（带缩进）
         */
        fun toMavenXml(indent: String = "    "): String {
            return buildString {
                appendLine("${indent}<dependency>")
                appendLine("$indent    <groupId>$groupId</groupId>")
                appendLine("$indent    <artifactId>$artifactId</artifactId>")
                appendLine("$indent    <version>$version</version>")
                scope?.let { appendLine("$indent    <scope>$it</scope>") }
                classifier?.let { appendLine("$indent    <classifier>$it</classifier>") }
                type?.let { appendLine("$indent    <type>$it</type>") }
                optional?.let { appendLine("$indent    <optional>$it</optional>") }
                append("${indent}</dependency>")
            }
        }
    }

    /**
     * 从 Maven XML 依赖标签中解析依赖坐标
     * 
     * @param dependencyXml Maven 依赖 XML 字符串
     * @return 解析后的依赖坐标，如果解析失败返回 null
     */
    fun parseDependency(dependencyXml: String): MavenDependencyCoordinate? {
        // 移除首尾空格
        val trimmed = dependencyXml.trim()
        
        // 提取各个标签的值
        val groupId = extractTagValue(trimmed, "groupId") ?: return null
        val artifactId = extractTagValue(trimmed, "artifactId") ?: return null
        val version = extractTagValue(trimmed, "version") ?: return null
        val scope = extractTagValue(trimmed, "scope")
        val classifier = extractTagValue(trimmed, "classifier")
        val type = extractTagValue(trimmed, "type")
        val optional = extractTagValue(trimmed, "optional")?.toBoolean()
        
        return MavenDependencyCoordinate(
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            scope = scope,
            classifier = classifier,
            type = type,
            optional = optional
        )
    }

    /**
     * 从单行格式解析依赖
     * 例如: <dependency><groupId>com.google.inject</groupId><artifactId>guice</artifactId><version>4.2.3</version></dependency>
     */
    fun parseDependencyFromCompactXml(compactXml: String): MavenDependencyCoordinate? {
        return parseDependency(compactXml)
    }

    /**
     * 从 Maven 坐标字符串解析
     * 格式: groupId:artifactId:version 或 groupId:artifactId:version:scope
     */
    fun parseDependencyFromCoordinate(coordinate: String): MavenDependencyCoordinate? {
        val parts = coordinate.split(":")
        if (parts.size < 3) return null
        
        return MavenDependencyCoordinate(
            groupId = parts[0],
            artifactId = parts[1],
            version = parts[2],
            scope = parts.getOrNull(3)
        )
    }

    /**
     * 更新依赖标签到最新版本
     * 
     * @param dependencyXml 原始 Maven 依赖 XML 字符串
     * @return 更新后的 XML 字符串，如果无法获取最新版本则返回原字符串
     */
    fun updateToLatestVersion(dependencyXml: String): String {
        val coordinate = parseDependency(dependencyXml) ?: return dependencyXml
        
        // 获取最新版本
        val latestVersion = MavenCentralSearchUtil.getLatestVersion(
            coordinate.groupId,
            coordinate.artifactId
        ) ?: return dependencyXml
        
        // 替换版本号
        return replaceVersion(dependencyXml, coordinate.version, latestVersion)
    }

    /**
     * 批量更新依赖标签到最新版本
     * 
     * @param dependencyXmlList 依赖 XML 字符串列表
     * @return 更新结果列表
     */
    fun batchUpdateToLatestVersion(dependencyXmlList: List<String>): List<UpdateResult> {
        return dependencyXmlList.map { original ->
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
     * 从完整的 pom.xml 内容中提取所有依赖标签
     * 
     * @param pomXml pom.xml 文件内容
     * @return 依赖标签列表
     */
    fun extractDependenciesFromPom(pomXml: String): List<String> {
        val dependencies = mutableListOf<String>()
        val pattern = Regex(
            """<dependency>.*?</dependency>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        pattern.findAll(pomXml).forEach { matchResult ->
            dependencies.add(matchResult.value)
        }
        
        return dependencies
    }

    /**
     * 更新整个 pom.xml 文件中的所有依赖到最新版本
     * 
     * @param pomXml 原始 pom.xml 内容
     * @return 更新后的 pom.xml 内容
     */
    fun updatePomToLatestVersions(pomXml: String): String {
        var updatedPom = pomXml
        val dependencies = extractDependenciesFromPom(pomXml)
        
        dependencies.forEach { dependency ->
            val updated = updateToLatestVersion(dependency)
            if (updated != dependency) {
                updatedPom = updatedPom.replace(dependency, updated)
            }
        }
        
        return updatedPom
    }

    /**
     * 获取 pom.xml 中所有依赖的更新报告
     * 
     * @param pomXml pom.xml 文件内容
     * @return 更新报告列表
     */
    fun getPomUpdateReport(pomXml: String): List<UpdateResult> {
        val dependencies = extractDependenciesFromPom(pomXml)
        return batchUpdateToLatestVersion(dependencies)
    }

    /**
     * 更新结果数据类
     */
    data class UpdateResult(
        val original: String,
        val updated: String,
        val isUpdated: Boolean,
        val coordinate: MavenDependencyCoordinate?
    ) {
        val oldVersion: String?
            get() = coordinate?.version
        
        val newVersion: String?
            get() = if (isUpdated) {
                parseDependency(updated)?.version
            } else {
                null
            }
        
        val summary: String
            get() = if (coordinate != null) {
                "${coordinate.groupId}:${coordinate.artifactId} " +
                if (isUpdated) {
                    "$oldVersion -> $newVersion"
                } else {
                    "$oldVersion (最新)"
                }
            } else {
                "解析失败"
            }
    }

    /**
     * 从 XML 中提取标签值
     */
    private fun extractTagValue(xml: String, tagName: String): String? {
        val pattern = Regex("<$tagName>\\s*([^<]+)\\s*</$tagName>")
        val matchResult = pattern.find(xml)
        return matchResult?.groupValues?.get(1)?.trim()
    }

    /**
     * 替换 XML 中的版本号
     */
    private fun replaceVersion(xml: String, oldVersion: String, newVersion: String): String {
        val pattern = Regex("(<version>)\\s*$oldVersion\\s*(</version>)")
        return pattern.replace(xml, "$1$newVersion$2")
    }

    /**
     * 验证依赖 XML 格式是否正确
     */
    fun isValidDependencyXml(dependencyXml: String): Boolean {
        return parseDependency(dependencyXml) != null
    }

    /**
     * 格式化依赖 XML（美化输出）
     */
    fun formatDependencyXml(dependencyXml: String, indent: String = "    "): String? {
        val coordinate = parseDependency(dependencyXml) ?: return null
        return coordinate.toMavenXml(indent)
    }

    /**
     * 从 Gradle 格式转换为 Maven 格式
     */
    fun convertFromGradle(gradleDependency: String, indent: String = "    "): String? {
        val coordinate = GradleDependencyParser.parseDependency(gradleDependency) ?: return null
        
        val mavenCoordinate = MavenDependencyCoordinate(
            groupId = coordinate.groupId,
            artifactId = coordinate.artifactId,
            version = coordinate.version,
            scope = when (coordinate.configuration) {
                "testImplementation", "testCompileOnly", "testRuntimeOnly" -> "test"
                "compileOnly" -> "provided"
                else -> null
            }
        )
        
        return mavenCoordinate.toMavenXml(indent)
    }
}
