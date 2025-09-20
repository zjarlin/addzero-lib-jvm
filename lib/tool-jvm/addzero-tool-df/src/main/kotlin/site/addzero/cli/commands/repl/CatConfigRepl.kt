package site.addzero.cli.commands.repl

import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.cli.config.configService
import site.addzero.core.ext.toJsonByKtx

/**
 * ConfigGetCommand的REPL包装类，实现AdvancedRepl接口
 */
@Single

class CatConfigRepl : AdvancedRepl<Unit, String> {
    override val command: String = "cat-config"
    override val description: String = "查看配置"
    override val paramDefs: List<ParamDef> = listOf()


    override fun eval(params: Unit): String =runBlocking {
        val toJsonByKtx = configService.config.toJsonByKtx()
        println("当前配置: ${System.lineSeparator()}")
        toJsonByKtx
//        println(toJsonByKtx).toString()
    }

    override fun createParams(values: List<Any?>) {
        return
    }
}
