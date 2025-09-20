package site.addzero.cli.commands.repl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.cli.dotfiles.dotfilesService

/**
 * SyncCommand的REPL包装类，实现AdvancedRepl接口
 */
@Single

class SyncCommandRepl : AdvancedRepl<Unit, Unit> {
    override val command: String = "sync"
    override val description: String = "同步dotfiles"
    override val paramDefs: List<ParamDef> = listOf()

    override fun eval(params: Unit)=runBlocking {

        withContext(Dispatchers.Default) {
            val success = dotfilesService.sync()
            if (success) {
                println("同步成功")
            } else {
                println("同步失败")
            }
        }
    }

    override fun createParams(values: List<Any?>) {
        return
    }
}
