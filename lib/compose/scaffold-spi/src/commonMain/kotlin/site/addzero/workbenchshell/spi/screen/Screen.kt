package site.addzero.workbenchshell.spi.screen

/**
 * 工作台菜单和页面元数据的统一协议。
 *
 * 这里故意沿用 Route 的字段语义，让注解路由、数据库菜单和其它来源都能收敛成同一种壳层输入。
 */
interface Screen {
    val value: String
    val title: String
    val routePath: String
    val icon: String
    val order: Double
    val enabled: Boolean
    val sceneName: String
    val sceneIcon: String
    val sceneOrder: Int
    val defaultInScene: Boolean
    val qualifiedName: String
    val simpleName: String
}

data class BasicScreen(
    override val value: String = "",
    override val title: String = "",
    override val routePath: String = "",
    override val icon: String = "Apps",
    override val order: Double = 0.0,
    override val enabled: Boolean = true,
    override val sceneName: String = "",
    override val sceneIcon: String = "Apps",
    override val sceneOrder: Int = Int.MAX_VALUE,
    override val defaultInScene: Boolean = false,
    override val qualifiedName: String = "",
    override val simpleName: String = "",
) : Screen

fun Screen.toBasicScreen(): BasicScreen {
    return when (this) {
        is BasicScreen -> this
        else -> BasicScreen(
            value = value,
            title = title,
            routePath = routePath,
            icon = icon,
            order = order,
            enabled = enabled,
            sceneName = sceneName,
            sceneIcon = sceneIcon,
            sceneOrder = sceneOrder,
            defaultInScene = defaultInScene,
            qualifiedName = qualifiedName,
            simpleName = simpleName,
        )
    }
}
