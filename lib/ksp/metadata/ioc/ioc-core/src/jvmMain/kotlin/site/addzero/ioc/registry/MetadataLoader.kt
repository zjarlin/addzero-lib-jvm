package site.addzero.ioc.registry

/**
 * 组件元数据
 * @param name 组件名称
 * @param className 类的全限定名
 */
data class ComponentMetadata(
    val name: String,
    val className: String
)

/**
 * 从 classpath 扫描并加载组件元数据
 *
 * 扫描所有 JAR 文件中的 META-INF/ioc-components.properties 文件
 */
object MetadataLoader {

    private const val METADATA_PATH = "META-INF/ioc-components.properties"

    /**
     * 加载所有模块的组件元数据
     * @return 组件元数据列表
     */
    fun loadComponentMetadata(): List<ComponentMetadata> {
        val result = mutableListOf<ComponentMetadata>()

        try {
            // 获取类加载器
            val classLoader = Thread.currentThread().contextClassLoader
                ?: MetadataLoader::class.java.classLoader

            // 查找所有 META-INF/ioc-components.properties 资源
            val resources = classLoader.getResources(METADATA_PATH)
            while (resources.hasMoreElements()) {
                val url = resources.nextElement()
                url.openStream().use { stream ->
                    val properties = java.util.Properties()
                    properties.load(stream)
                    properties.forEach { (key, value) ->
                        result.add(ComponentMetadata(key as String, value as String))
                    }
                }
            }

            // 也尝试从当前线程的类加载器获取
            val threadResources = Thread.currentThread().contextClassLoader?.getResources(METADATA_PATH)
            threadResources?.let {
                while (it.hasMoreElements()) {
                    val url = it.nextElement()
                    // 避免重复处理
                    if (!result.any { it.className == url.toString() }) {
                        url.openStream().use { stream ->
                            val properties = java.util.Properties()
                            properties.load(stream)
                            properties.forEach { (key, value) ->
                                result.add(ComponentMetadata(key as String, value as String))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 如果扫描失败，返回空列表
            // 日志输出
            System.err.println("Warning: Failed to load component metadata: ${e.message}")
        }

        return result
    }

    /**
     * 根据组件名称加载元数据
     * @param name 组件名称
     * @return 组件元数据，如果不存在则返回 null
     */
    fun loadByName(name: String): ComponentMetadata? {
        return loadComponentMetadata().firstOrNull { it.name == name }
    }

    /**
     * 根据类名加载元数据
     * @param className 类的全限定名
     * @return 组件元数据，如果不存在则返回 null
     */
    fun loadByClassName(className: String): ComponentMetadata? {
        return loadComponentMetadata().firstOrNull { it.className == className }
    }
}
