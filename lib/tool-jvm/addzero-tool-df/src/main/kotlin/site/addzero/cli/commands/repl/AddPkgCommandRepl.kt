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
class AddPkgCommandRepl : AdvancedRepl<AddPkgCommandRepl.AddPkgCommandParams, Unit> {
    override val command: String = "add-pkg"
    override val description: String = "添加当前操作系统的包管理系统软件包"
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
        //todo 验证在不在包管理器内
        val addPkg = configService.addPkg(packageName)
        if (addPkg) {
            // 上传配置
            SyncUtil.commitAndPush()
            println("已添加软件包: $packageName")
        } else {
            System.err. println("添加软件包出错了")
        }
    }

    override fun createParams(values: List<Any?>): AddPkgCommandParams {
        return AddPkgCommandParams(
            packageName = values[0] as String
        )
    }
}
