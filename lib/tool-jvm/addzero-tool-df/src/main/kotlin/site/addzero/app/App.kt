
package site.addzero.app
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ksp.generated.defaultModule
import site.addzero.cli.commands.ReplTemp
import kotlin.getValue

fun main() {
    // 启动Koin并加载模块
    startKoin {
        modules(defaultModule)
    }
    startReplMode()

//    CommandApp(args).runApp()
}

private fun startReplMode() {

   val replTemp: ReplTemp by inject(ReplTemp::class.java)
    val repls = replTemp.repls

    @Suppress("UNCHECKED_CAST")
    (repls as List<AdvancedRepl<Any, Any>>).toAdvancedRepl(
        prompt = "dotfiles-cli > ",
        exitCommand = "q",
        helpCommand = "h"
    )
}
