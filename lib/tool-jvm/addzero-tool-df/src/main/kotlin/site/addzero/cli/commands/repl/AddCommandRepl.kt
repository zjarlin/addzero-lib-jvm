package site.addzero.cli.commands.repl

import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.cli.config.Lines
import site.addzero.cli.config.configService
import site.addzero.cli.dotfiles.sync_stragty.SyncUtil
import site.addzero.cli.setting.SettingContext.DOTFILES_DIR
import site.addzero.cli.setting.SettingContext.HOME_DIR
import site.addzero.util.AddFileUtil
import java.io.File
import kotlin.reflect.typeOf


/**
 * AddCommand的REPL包装类，实现AdvancedRepl接口
 */
@Single
class AddCommandRepl : AdvancedRepl<AddCommandRepl.AddCommandParams, Unit> {
    override val command: String = "add-dotfiles"
    override val description: String =
        "添加文件到dotfiles并在原位置留下软链接(默认为相对家目录${HOME_DIR}路径,开启绝对路径请使用-a false参数)"
    override val paramDefs: List<ParamDef> = listOf(
        ParamDef(
            name = "sourcePath",
            type = typeOf<String>(),
            description = "要添加的文件路径"
        ),
        ParamDef(
            name = "abs",
            type = typeOf<Boolean>(),
            description = "是否使用绝对路径(默认为相对家目录${HOME_DIR}路径)",
            defaultValue = false
        ),
    )

    data class AddCommandParams(
        val sourcePath: String,
        val abs: Boolean = false,
    )

    override fun eval(params: AddCommandParams) = runBlocking {
        val abs = params.abs
        val sourcePath = params.sourcePath
        val currentPlatformConfig = configService.osConfig

        val newSourceDirAbs = if (!abs) {
            val file = File(HOME_DIR, sourcePath)
            if (!file.exists()) {
                null
            }
            val absolutePath = file.absolutePath
            absolutePath
        } else {
            val file = File(sourcePath)
            if (!file.exists()) {
                null
            }
            sourcePath
        }
        if (newSourceDirAbs == null) {
            System.err.println("dotfiles文件不存在")
            return@runBlocking  // 直接返回，终止当前协程
        }
        val newtargetDir = if (abs) {
            //弃用了绝对路径,那么需要获取源文件的
            DOTFILES_DIR
        } else {
            File(DOTFILES_DIR, newSourceDirAbs.substringAfter(HOME_DIR))
        }

        println("开始创建软链接,${newSourceDirAbs} to ${DOTFILES_DIR}")

        val success = AddFileUtil.mvln(newSourceDirAbs, DOTFILES_DIR)
        //提交配置
        val lines = currentPlatformConfig.links.plus(Lines(newSourceDirAbs, DOTFILES_DIR))
        val copy = currentPlatformConfig.copy(links = lines)
        configService.updatePlatFormConfig(copy)

        // 上传配置
        SyncUtil.commitAndPush()

        println("文件添加完成")
    }

    override fun createParams(values: List<Any?>): AddCommandParams {
        return AddCommandParams(
            sourcePath = values[0] as String,
            abs = values.getOrNull(1) as? Boolean ?: false,
        )
    }
}
