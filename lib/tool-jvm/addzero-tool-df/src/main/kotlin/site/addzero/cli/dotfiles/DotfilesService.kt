package site.addzero.cli.dotfiles

import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject
import site.addzero.cli.config.ConfigService
import site.addzero.cli.dotfiles.sync_stragty.SyncUtil
import site.addzero.cli.setting.SettingContext
import site.addzero.ioc.annotation.Bean
import java.io.File

val dotfilesService: DotfilesService by inject(DotfilesService::class.java)

/**
 * Dotfiles管理器
 *
 * 负责dotfiles的同步和软链接操作
 */
@Single(createdAtStart = true)
class DotfilesService(private val configService: ConfigService) {


    init {
        /**
         * 初始化dotfiles目录
         *
         * @param gitRepo Git仓库URL，如果提供则从该仓库克隆
         * @return 是否初始化成功
         */
        @Bean

        fun init(): Boolean = run {
            val dotfilesDirectory = File(SettingContext.DOTFILES_DIR)
            // 检查目录是否已存在
            if (dotfilesDirectory.exists()) {
                println("Dotfiles目录已存在: ${SettingContext.DOTFILES_DIR} 无需创建")
                return true
            }

            // 创建目录
            if (!dotfilesDirectory.mkdirs()) {
                println("无法创建Dotfiles目录: ${SettingContext.DOTFILES_DIR}")
                return false
            }
            val handler = SyncUtil.pull()
            if (handler) {
                println("同步dotfiles成功")
                false
            } else {
                println("同步dotfiles失败")
                true
            }


        }
        init()
    }

    val gitRepo = configService.config.cloudUrl


    fun sync(): Boolean {
        //todo 检查配置文件在不在,检查有没有关联上游如果关联了就拉取最新的
        if (true) {
            SyncUtil.pull()
            SyncUtil.commitAndPush()
        }
        //然后批量根据links创建符号链接即可
        return TODO("提供返回值")
    }

}
