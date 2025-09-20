package site.addzero.cli.dotfiles.sync_stragty

import ensureExists
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.cli.config.ConfigService
import site.addzero.cli.config.SyncType
import site.addzero.cli.config.configService
import site.addzero.cli.dotfiles.sync_stragty.SyncUtil.pull
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.runCmd
import site.addzero.cli.setting.SettingContext
import java.io.File

@Single(createdAtStart = true)
class GitAutoInit {
    init {
        if (configService.config.syncType == SyncType.GIT) {
            pull()
        }
    }


}

@Single
class GitSyncStrategy(private val configService: ConfigService) : SyncStragty {

    private val config = configService.config
    private val cloudUrl = config.cloudUrl

    override val support: Boolean
        get() {
            // 检查是否配置了Git同步以及是否有云端仓库地址
            return config.syncType == SyncType.GIT
        }

    override fun pull(): Boolean {
        if (gitUrlNotSetFlag()) return false
        // 检查本地dotfiles目录是否已存在
        val dotfilesDir = File(SettingContext.SYNC_DIR)
        return if (dotfilesDir.exists() && dotfilesDir.isDirectory) {
            // 如果目录已存在，尝试更新
            updateExistingRepo()
        } else {
            // 如果目录不存在，进行克隆
            cloneRepo()
        }
    }

    override fun commitAndPush(): Boolean {
        if (gitUrlNotSetFlag()) return false

        val dotfilesDir = File(SettingContext.SYNC_DIR)
        if (!dotfilesDir.exists() || !dotfilesDir.isDirectory) {
            println("同步目录不存在: ${SettingContext.SYNC_DIR}")
            return false
        }

        println("正在检查本地更改...")
        checkGitIgnore()

        // 检查是否有更改需要提交
        val statusResult = "cd ${SettingContext.SYNC_DIR} && git status --porcelain".runCmd()
        if (statusResult.exitCode != 0) {
            System.err.println("检查Git状态失败: ${statusResult.output}")
            return false
        }

        if (statusResult.output.trim().isEmpty()) {
            println("没有需要提交的更改")
            return true
        }

        println("检测到以下更改:")
        println(statusResult.output)

        // 添加所有更改到暂存区
        val addResult = "cd ${SettingContext.SYNC_DIR} && git add .".runCmd()
        if (addResult.exitCode != 0) {
            System.err.println("添加文件到暂存区失败: ${addResult.output}")
            return false
        }

        // 提交更改
        val commitResult = "cd ${SettingContext.SYNC_DIR} && git commit -m \"Update dotfiles\"".runCmd()
        if (commitResult.exitCode != 0) {
            // 检查是否是因为没有更改导致的提交失败
            if (commitResult.output.contains("nothing to commit")) {
                System.err.println("没有需要提交的更改")
                return true
            }
            System.err.println("提交更改失败: ${commitResult.output}")
            return false
        }

        println("成功提交更改")

        // 推送到远程仓库
        println("正在推送到远程仓库...")
//          git config pull.rebase false


        println("尝试更新仓库")
        val pullResult = "cd ${SettingContext.SYNC_DIR} &&git pull origin master".runCmd()
        if (pullResult.isError()) {
            System.err.println("拉取远程仓库失败,尝试合并")
            val mergeResult = "cd ${SettingContext.SYNC_DIR} &&git config pull.rebase false".runCmd()
            if (mergeResult.isError()) {
                System.err.println("合并失败:")
                return false
            }
        }
        val pushResult = "cd ${SettingContext.SYNC_DIR} &&git push origin master".runCmd()
        if (pushResult.isError()) {
            System.err.println("推送更改失败:")
            System.err.println("推送错误原因: ${pushResult.output}")
            return false
        }
        println("成功推送更改到远程仓库")
        return true
    }

    private fun checkGitIgnore() {
        val file = File(SettingContext.SYNC_DIR, ".gitignore")
        if (file.exists()) {
            return
        }
        val ensureExists = file.ensureExists()
        ensureExists.writeText(
            """
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

### Kotlin ###
.kotlin

### Eclipse ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

### VS Code ###
.vscode/

### Mac OS ###
.DS_Store 
        """.trimIndent()
        )
    }

    fun gitUrlNotSetFlag(): Boolean {
        // 检查是否支持Git同步
        if (!support) {
            println("当前配置的同步类型不是GIT")
            return true
        }

        if (cloudUrl == null) {
            val input = PlatformService.readLine("请先设置云端仓库地址") ?: ""
            val runBlocking = runBlocking {
                configService.updateConfig(config.copy(cloudUrl = input))
                println("已设置云端仓库地址为: $input")
                true
            }
        }
        return false
    }

    /**
     * 克隆远程仓库
     */
    private fun cloneRepo(): Boolean {
        println("正在从 $cloudUrl 克隆dotfiles...")
        val result = "git clone $cloudUrl ${SettingContext.SYNC_DIR}".runCmd()
        if (result.exitCode != 0) {
            System.err.println("克隆Git仓库失败: ${result.output}")
            return false
        }
        System.err.println("成功从 $cloudUrl 克隆dotfiles")
        return true
    }

    /**
     * 更新现有仓库
     */
    private fun updateExistingRepo(): Boolean {
        println("本地dotfiles目录已存在，正在检查远程仓库关联...")

        // 检查是否已经关联了远程仓库
        val remoteCheckResult = "cd ${SettingContext.SYNC_DIR} && git remote get-url origin".runCmd()

        return when {
            // 如果有远程仓库关联
            remoteCheckResult.exitCode == 0 -> {
                println("检测到已关联远程仓库: ${remoteCheckResult.output.trim()}")
                // 检查关联的远程仓库是否与配置的仓库一致
                if (remoteCheckResult.output.trim() == cloudUrl) {
                    println("远程仓库匹配，正在更新...")
                    val pullResult = "cd ${SettingContext.SYNC_DIR} && git pull".runCmd()
                    if (pullResult.exitCode == 1) {
                        println("为此分支创建跟踪信息")
                        val runCmd1 = """ git branch --set-upstream-to=origin/master master """.trimIndent().runCmd()
                        if (runCmd1.exitCode != 0) {
                            System.err.println("更新Git仓库失败: ${pullResult.output}")
                            false
                        } else {
                            println("成功更新同步目录")
                            true
                        }

                    } else {
                        println("成功更新同步目录")
                        true
                    }
                } else {
                    println("关联的远程仓库与配置不匹配:")
                    println("  配置的仓库: $cloudUrl")
                    println("  关联的仓库: ${remoteCheckResult.output.trim()}")
                    println("请手动处理仓库冲突或删除现有目录后重试")
                    false
                }
            }

            // 如果没有远程仓库关联
            remoteCheckResult.exitCode != 0 -> {
                System.err.println("本地仓库未关联远程仓库，正在关联并更新...")
                // 添加远程仓库关联
                val addRemoteResult =
                    "cd ${SettingContext.SYNC_DIR} &&git init&& git remote add origin $cloudUrl".runCmd()
                if (addRemoteResult.exitCode != 0) {
                    System.err.println("关联远程仓库失败: ${addRemoteResult.output}")
                    return false
                }

                // 获取远程仓库的更改
                val fetchResult = "cd ${SettingContext.SYNC_DIR} && git fetch origin && git pull".runCmd()
                if (fetchResult.exitCode != 0) {
                    System.err.println("获取远程仓库更改失败: ${fetchResult.output}")
                    return false
                }
                println("成功关联远程仓库并更新dotfiles")
                true
            }

            else -> {
                println("检查远程仓库关联时发生未知错误")
                false
            }
        }
    }

    override val syncType: SyncType
        get() = SyncType.GIT
}
