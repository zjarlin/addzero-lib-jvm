package site.addzero.util.ddlgenerator.koin

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * DDL Generator Koin 模块
 * 
 * 使用 Koin 注解自动扫描和注册所有方言实现
 * 只需要在方言类上添加 @Single 注解即可自动注册
 */
@Module
@ComponentScan("site.addzero.util.ddlgenerator.dialect")
class DdlGeneratorModule
