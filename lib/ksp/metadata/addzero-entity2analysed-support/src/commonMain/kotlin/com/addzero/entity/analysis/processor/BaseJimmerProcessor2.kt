//package com.addzero.entity.analysis.processor
//
//import com.addzero.context.SettingContext
//import com.addzero.entity.analysis.JimmerEntityAnalyzer
//import com.addzero.entity.analysis.model.EntityMetadata
//import com.addzero.kaleidoscope.core.KldElement
//import com.addzero.kaleidoscope.core.KldResolver
//import com.addzero.kaleidoscope.core.KldTypeElement
//import com.addzero.kaleidoscope.core.getPlatformSpecificElementOrNull
//import com.addzero.kaleidoscope.ksp.toKldResolver
//import com.google.devtools.ksp.processing.Resolver
//import com.google.devtools.ksp.processing.SymbolProcessor
//import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
//import com.google.devtools.ksp.symbol.KSAnnotated
//import com.google.devtools.ksp.symbol.KSClassDeclaration
//
///**
// * Jimmer 处理器基类 (KLD实现版本)
// *
// * 提供统一的 Jimmer 实体元数据收集流程，使用KLD抽象接口实现：
// * 1. 使用KLD解析器查找 Jimmer 实体
// * 2. 收集实体元数据
// * 3. 子类实现具体的处理逻辑
// *
// * 注意：此基类只负责元数据收集，不涉及具体的代码生成逻辑
// */
//abstract class BaseJimmerProcessor2(
//    protected val environment: SymbolProcessorEnvironment,
//) : SymbolProcessor {
//
//    protected val logger = environment.logger
//    protected val options = environment.options
//
//    // Jimmer 实体分析器
//    protected val entityAnalyzer = JimmerEntityAnalyzer(logger)
//
//    // 存储收集到的实体元数据
//    protected val collectedEntities = mutableListOf<EntityMetadata>()
//
//    // 存储原始的实体声明（用于需要访问 KSPropertyDeclaration 的处理器）
//    protected val entityDeclarations = mutableMapOf<String, KSClassDeclaration>()
//
//    // 存储KLD元素（用于KLD处理）
//    protected val kldEntityElements = mutableListOf<KldElement>()
//
//    override fun process(resolver: Resolver): List<KSAnnotated> {
//        SettingContext.initialize(options)
//
//        // 使用KLD解析器查找所有 Jimmer 实体
//        val kldResolver = resolver.toKldResolver(environment = environment)
//
//        val entityElements = kldResolver.getElementsAnnotatedWith("org.babyfish.jimmer.sql.Entity")
//
//        logger.warn("找到 ${entityElements.count()} 个 Jimmer 实体")
//
//        if (entityElements.count() == 0) {
//            logger.warn("没有找到任何 Jimmer 实体，退出处理")
//            return emptyList()
//        }
//
//        // 第一阶段：收集实体元数据
//        entityElements.forEach { entityElement ->
//            logger.warn("收集实体元数据: ${entityElement.simpleName}")
//            try {
//                // 保存KLD元素
//                kldEntityElements.add(entityElement)
//
//                // 直接使用KLD元素进行分析
//                val metadata = entityAnalyzer.analyzeEntity(entityElement)
//                collectedEntities.add(metadata)
//
//                // 如果是类型元素，保存原始的KSClassDeclaration
//                if (entityElement is KldTypeElement) {
//                    val ksClassDeclaration = getOriginalKSClassDeclaration(entityElement)
//                    if (ksClassDeclaration != null) {
//                        // 保存原始实体声明
//                        entityDeclarations[metadata.qualifiedName] = ksClassDeclaration
//                    }
//                }
//            } catch (e: Exception) {
//                logger.error("收集实体元数据失败: ${entityElement.simpleName}, 错误: ${e.message}")
//            }
//        }
//
//        logger.warn("元数据收集完成，共收集 ${collectedEntities.size} 个实体")
//        return emptyList() // KLD处理不返回需要重新处理的符号
//    }
//
//    /**
//     * 通过KLD类型元素获取原始的KSClassDeclaration
//     *
//     * 使用KLD扩展函数实现，不依赖任何平台特定API
//     */
//    private fun getOriginalKSClassDeclaration(kldTypeElement: KldTypeElement): KSClassDeclaration? {
//        return try {
//            // 使用KLD扩展函数安全地获取原始元素
//            kldTypeElement.getPlatformSpecificElementOrNull<KSClassDeclaration>()
//        } catch (e: Exception) {
//            // 记录警告但不中断执行
//            logger.warn("获取原始KSClassDeclaration时发生错误: ${e.message}")
//            null
//        }
//    }
//
//    override fun finish() {
//        if (collectedEntities.isEmpty()) {
//            logger.warn("没有收集到实体元数据，跳过处理")
//            return
//        }
//
//        logger.warn("开始处理收集到的实体元数据...")
//
//        try {
//            // 调用子类的处理逻辑
//            processEntities(collectedEntities)
//
//            logger.warn("实体元数据处理完成")
//        } catch (e: Exception) {
//            logger.error("实体元数据处理失败: ${e.message}")
//            throw e
//        }
//    }
//
//    /**
//     * 处理实体元数据
//     * 子类必须实现此方法来定义具体的处理逻辑（如代码生成等）
//     */
//    protected abstract fun processEntities(entities: List<EntityMetadata>)
