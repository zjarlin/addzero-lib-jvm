package site.addzero.cli.commands.repl

import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.cli.config.configService
import site.addzero.cli.dotfiles.sync_stragty.SyncUtil
import kotlin.reflect.typeOf

/**
 * AddPkgCommand的REPL包装类，实现AdvancedRepl接口
 */
@Single

class RmPkgCommandRepl : AdvancedRepl<RmPkgCommandRepl.AddPkgCommandParams, Unit> {
    override val command: String = "rm-pkg"
    override val description: String = "删除件包(注意并不会删除操作系统软件,只是换一台电脑不会同步该软件)"
    override val paramDefs: List<ParamDef> = listOf(
        ParamDef(
            name = "packageName",
            type = typeOf<String>(),
            description = "软件包名称",
        )
    )

    data class AddPkgCommandParams(
        val packageName: String
    )

    override fun eval(params: AddPkgCommandParams) =runBlocking {
        val packageName = params.packageName
        val rmPkg = configService.rmPkg(packageName)
        if (rmPkg) {
            // 上传配置
            SyncUtil.commitAndPush()
            println("已删除软件包: $packageName")
        } else {
           System.err. println("删除软件包出错了")
        }
    }

    override fun createParams(values: List<Any?>): AddPkgCommandParams {
        return AddPkgCommandParams(
            packageName = values[0] as String
        )
    }
}
