package site.addzero.annotation

/**
 * 场景元数据。
 */
@Retention(AnnotationRetention.SOURCE)
annotation class RouteScene(
    val name: String = "",
    val icon: String = "Apps",
    val order: Int = Int.MAX_VALUE,
)

/**
 * 路由落点元数据。
 */
@Retention(AnnotationRetention.SOURCE)
annotation class RoutePlacement(
    val scene: RouteScene = RouteScene(),
    val defaultInScene: Boolean = false,
)

/**
 * 路由注解
 * 用于生成路由表
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
//    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.SOURCE)
annotation class Route(
    val value: String = "",      // 父级名称, 为空时挂到场景根节点
    val title: String = "",      // 路由标题
    val routePath: String = "",  // 为空时使用全限定名
    val icon: String = "Apps",   // 页面图标
    val order: Double = 0.0,     // 页面排序(支持小数,方便插入新项)
    val placement: RoutePlacement = RoutePlacement(),
    val qualifiedName: String = "",
    val simpleName: String = "",
)
