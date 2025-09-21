package site.addzero.cli.setting
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
    //可以改
    val WORK_DIR = "${HOME_DIR}/.config/df"

    //可以改
    val SYNC_DIR: String = "${WORK_DIR}/dfctx"

    val STATUS_DIR = "$WORK_DIR/cache/.status"
    val STATUS_FILE = "$STATUS_DIR/task_status.json"

    var CONFIG_FILE = "$SYNC_DIR/config.json"
    val DOTFILES_DIR = "$SYNC_DIR/.dotfiles"


    val DEFAULT_PKG: Set<String> = linkedSetOf("git", "curl", "wget", "zsh", "neovim", "node", "npm", "yarn")


}

