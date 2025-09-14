//package com.addzero.web.event
//
//import com.addzero.common.consts.sql
//import com.addzero.generated.mcp.SysTableConfigMcpService
//import com.addzero.model.entity.SysTableConfig
//import com.addzero.util.JdbcMetadataExtractor
//import com.addzero.web.infra.jimmer.dynamicdatasource.DynamicDatasourceProperties
//import org.babyfish.jimmer.sql.ast.mutation.SaveMode
//import org.springframework.context.event.ContextRefreshedEvent
//import org.springframework.context.event.EventListener
//import org.springframework.core.annotation.Order
//import org.springframework.scheduling.annotation.Async
//import org.springframework.stereotype.Component
//
//@Component
//@Order(999) // 确保在Flyway之后执行
//class JdbcMetaInitEvent(
//    private val dynamicDatasourceProperties: DynamicDatasourceProperties,
//    private val sysTableConfigMcpService: SysTableConfigMcpService
//) {
//    @EventListener(ContextRefreshedEvent::class)
//    @Async
//    fun initJdbcMetaData() {
//        val primary = dynamicDatasourceProperties.primary
//        val config = dynamicDatasourceProperties.datasource[primary]!!
//        val jdbcUrl = config.url.split("?").first()
//        val jdbcSchema = config.url.split("?").last().split("=").last()
//        val extractDatabaseMetadata = JdbcMetadataExtractor.extractDatabaseMetadata(
//            JdbcMetadataExtractor
//                .JdbcConfig(
//                    jdbcUrl = jdbcUrl,
//                    jdbcUsername = config.username,
//                    jdbcPassword = config.password,
//                    jdbcSchema = jdbcSchema,
//                    jdbcDriver = config.driverClassName,
//                    excludeTables = config.excludeTables.split(",")
//                )
//        )
//
//        val map = extractDatabaseMetadata.map {
//            SysTableConfig {
//                routeKey = ""
//                showPagination = true
//                showSearchBar = true
//                showBatchActions = true
//                showRowSelection = true
//                showDefaultRowActions = true
//                enableSorting = true
//                enableAdvancedSearch = true
//                headerHeightDp = TODO()
//                rowHeightDp = TODO()
//            }
//        }
//        sql.saveEntities(
//            map,
//            mode = SaveMode.INSERT_IF_ABSENT
//        )
//
//    }
//}
