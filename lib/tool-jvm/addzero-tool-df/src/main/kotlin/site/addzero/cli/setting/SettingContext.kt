package site.addzero.cli.setting

import site.addzero.ioc.annotation.Bean


object SettingContext {
    val HOME_DIR
        get() = run {
            val property = System.getProperty("user.home")
            if (property.contains("/Users/zjarlin/IdeaProjects")) {
                "/Users/zjarlin"
            } else {
                property
            }
        }
    val WORK_DIR = "${HOME_DIR}/.config/df"
    val SYNC_DIR: String
        get() = "${WORK_DIR}/dfctx"
    val STATUS_DIR = "$WORK_DIR/cache/.status"
    val STATUS_FILE = "$STATUS_DIR/task_status.json"

    var CONFIG_FILE = "$SYNC_DIR/config.json"
    val DOTFILES_DIR = "$SYNC_DIR/.dotfiles"


    val DEFAULT_PKG: Set<String> = linkedSetOf("git", "curl", "wget", "zsh", "neovim", "node", "npm", "yarn")

    const val EXIT_COMMAND = "q"

    const val HELP_COMMAND = "h"

}

