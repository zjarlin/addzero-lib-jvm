package com.addzero.kmp.kaleidoscope.core

/**
 * Kaleidoscope 平台兼容性工具
 *
 * 提供平台检测、能力查询和安全转换功能
 */
object KldCompatibility {

    /**
     * 平台类型枚举
     */
    enum class KldPlatform {
        KSP,    // Kotlin Symbol Processing
        APT,    // Annotation Processing Tool
        UNKNOWN
    }

    /**
     * 功能兼容性级别
     */
    enum class KldCompatibilityLevel {
        FULL_SUPPORT,       // 完全支持
        LIMITED_SUPPORT,    // 有限支持
        NOT_SUPPORTED      // 不支持，会抛出异常
    }

    /**
     * 检测当前运行的平台
     */
    fun detectPlatform(resolver: KldResolver): KldPlatform {
        return try {
            // 尝试KSP特有的特性
            resolver.getAllFiles().firstOrNull()
            if (!resolver.isProcessingOver) {
                KldPlatform.KSP
            } else {
                KldPlatform.APT
            }
        } catch (e: UnsupportedOperationException) {
            // getAllFiles抛异常说明是APT平台
            KldPlatform.APT
        } catch (e: Exception) {
            KldPlatform.UNKNOWN
        }
    }

    /**
     * 检查特定功能的兼容性
     */
    fun checkCompatibility(resolver: KldResolver, kldFeature: KldFeature): KldCompatibilityLevel {
        val platform = detectPlatform(resolver)
        return when (kldFeature) {
            KldFeature.GET_ELEMENTS_ANNOTATED_WITH -> KldCompatibilityLevel.FULL_SUPPORT
            KldFeature.GET_CLASS_DECLARATION -> KldCompatibilityLevel.FULL_SUPPORT
            KldFeature.CREATE_SOURCE_FILE -> KldCompatibilityLevel.FULL_SUPPORT
            KldFeature.LOGGING -> KldCompatibilityLevel.FULL_SUPPORT
            KldFeature.GET_OPTIONS -> KldCompatibilityLevel.FULL_SUPPORT

            KldFeature.GET_PACKAGE_DECLARATION -> when (platform) {
                KldPlatform.APT -> KldCompatibilityLevel.FULL_SUPPORT
                KldPlatform.KSP -> KldCompatibilityLevel.NOT_SUPPORTED
                KldPlatform.UNKNOWN -> KldCompatibilityLevel.LIMITED_SUPPORT
            }

            KldFeature.GET_ALL_FILES -> when (platform) {
                KldPlatform.KSP -> KldCompatibilityLevel.FULL_SUPPORT
                KldPlatform.APT -> KldCompatibilityLevel.NOT_SUPPORTED
                KldPlatform.UNKNOWN -> KldCompatibilityLevel.LIMITED_SUPPORT
            }

            KldFeature.IS_PROCESSING_OVER -> when (platform) {
                KldPlatform.APT -> KldCompatibilityLevel.FULL_SUPPORT
                KldPlatform.KSP -> KldCompatibilityLevel.LIMITED_SUPPORT
                KldPlatform.UNKNOWN -> KldCompatibilityLevel.LIMITED_SUPPORT
            }
        }
    }

    /**
     * 功能枚举
     *
     * 定义Kaleidoscope支持的各种功能点，用于兼容性检查和平台适配
     */
    enum class KldFeature {
        /** 获取被注解标记的元素 */
        GET_ELEMENTS_ANNOTATED_WITH,

        /** 获取类声明 */
        GET_CLASS_DECLARATION,

        /** 获取包声明 */
        GET_PACKAGE_DECLARATION,

        /** 获取所有源文件 */
        GET_ALL_FILES,

        /** 创建源文件 */
        CREATE_SOURCE_FILE,

        /** 日志记录功能 */
        LOGGING,

        /** 获取编译器选项 */
        GET_OPTIONS,

        /** 检查处理是否完成 */
        IS_PROCESSING_OVER
    }

    /**
     * 安全执行包相关操作
     */
    fun safeGetPackageDeclaration(
        resolver: KldResolver,
        qualifiedName: String,
        onSuccess: (KldPackageElement) -> Unit = {},
        onNotSupported: () -> Unit = {}
    ) {
        try {
            val packageElement = resolver.getPackageDeclaration(qualifiedName)
            if (packageElement != null) {
                onSuccess(packageElement)
            }
        } catch (e: UnsupportedOperationException) {
            resolver.warn("当前平台不支持包声明获取: ${e.message}")
            onNotSupported()
        }
    }

    /**
     * 安全执行文件列表操作
     */
    fun safeGetAllFiles(
        resolver: KldResolver,
        onSuccess: (Sequence<KldSourceFile>) -> Unit = {},
        onNotSupported: () -> Unit = {}
    ) {
        try {
            val files = resolver.getAllFiles()
            onSuccess(files)
        } catch (e: UnsupportedOperationException) {
            resolver.warn("当前平台不支持获取所有文件: ${e.message}")
            onNotSupported()
        }
    }

    /**
     * 平台能力信息
     */
    data class PlatformCapabilities(
        val kldPlatform: KldPlatform,
        val supportedKldFeatures: Set<KldFeature>,
        val limitedKldFeatures: Set<KldFeature>,
        val unsupportedKldFeatures: Set<KldFeature>
    )

    /**
     * 获取平台完整能力信息
     */
    fun getPlatformCapabilities(resolver: KldResolver): PlatformCapabilities {
        val platform = detectPlatform(resolver)
        val allKldFeatures = KldFeature.entries.toTypedArray()

        val supported = mutableSetOf<KldFeature>()
        val limited = mutableSetOf<KldFeature>()
        val unsupported = mutableSetOf<KldFeature>()

        allKldFeatures.forEach { feature ->
            when (checkCompatibility(resolver, feature)) {
                KldCompatibilityLevel.FULL_SUPPORT -> supported.add(feature)
                KldCompatibilityLevel.LIMITED_SUPPORT -> limited.add(feature)
                KldCompatibilityLevel.NOT_SUPPORTED -> unsupported.add(feature)
            }
        }

        return PlatformCapabilities(platform, supported, limited, unsupported)
    }

    /**
     * 打印平台兼容性报告
     */
    fun printCompatibilityReport(resolver: KldResolver) {
        val capabilities = getPlatformCapabilities(resolver)

        resolver.info("=== Kaleidoscope 平台兼容性报告 ===")
        resolver.info("检测到平台: ${capabilities.kldPlatform}")
        resolver.info("完全支持的功能 (${capabilities.supportedKldFeatures.size}个):")
        capabilities.supportedKldFeatures.forEach { feature ->
            resolver.info("  ✅ ${feature.name}")
        }

        if (capabilities.limitedKldFeatures.isNotEmpty()) {
            resolver.info("有限支持的功能 (${capabilities.limitedKldFeatures.size}个):")
            capabilities.limitedKldFeatures.forEach { feature ->
                resolver.info("  ⚠️ ${feature.name}")
            }
        }

        if (capabilities.unsupportedKldFeatures.isNotEmpty()) {
            resolver.info("不支持的功能 (${capabilities.unsupportedKldFeatures.size}个):")
            capabilities.unsupportedKldFeatures.forEach { feature ->
                resolver.info("  ❌ ${feature.name}")
            }
        }

        resolver.info("================================")
    }
}

/**
 * KldResolver 扩展函数 - 兼容性相关
 */

/**
 * 检测当前平台
 */
fun KldResolver.detectPlatform(): KldCompatibility.KldPlatform {
    return KldCompatibility.detectPlatform(this)
}

/**
 * 检查功能兼容性
 */
fun KldResolver.checkCompatibility(kldFeature: KldCompatibility.KldFeature): KldCompatibility.KldCompatibilityLevel {
    return KldCompatibility.checkCompatibility(this, kldFeature)
}

/**
 * 是否为KSP平台
 */
fun KldResolver.isKspPlatform(): Boolean {
    return detectPlatform() == KldCompatibility.KldPlatform.KSP
}

/**
 * 是否为APT平台
 */
fun KldResolver.isAptPlatform(): Boolean {
    return detectPlatform() == KldCompatibility.KldPlatform.APT
}

/**
 * 安全执行可能不兼容的操作
 */
inline fun <T> KldResolver.safeExecute(
    operation: () -> T,
    onUnsupported: (String) -> T
): T {
    return try {
        operation()
    } catch (e: UnsupportedOperationException) {
        warn("操作不支持: ${e.message}")
        onUnsupported(e.message ?: "未知错误")
    }
}

/**
 * 跨平台安全的包名获取
 */
fun KldResolver.safeGetPackageName(element: KldElement): String? {
    return safeExecute(
        operation = { element.packageName },
        onUnsupported = { null }
    )
}

/**
 * 带平台检测的批量处理
 */
fun KldResolver.processElementsCrossPlatform(
    qualifiedName: String,
    processor: (KldElement, KldCompatibility.KldPlatform) -> Unit
) {
    val platform = detectPlatform()
    getElementsAnnotatedWith(qualifiedName).forEach { element ->
        processor(element, platform)
    }
}
