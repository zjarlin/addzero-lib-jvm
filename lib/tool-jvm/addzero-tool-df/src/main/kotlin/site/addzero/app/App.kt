package site.addzero.app
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.get as getKoinContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ksp.generated.defaultModule
import site.addzero.cli.commands.ReplTemp

fun main() {
    // 启动Koin并加载模块
    val startKoin = startKoin {
        modules(defaultModule)
    }
    val replTemp: ReplTemp by inject(ReplTemp::class.java)
    replTemp.repls.startReplMode()
}

