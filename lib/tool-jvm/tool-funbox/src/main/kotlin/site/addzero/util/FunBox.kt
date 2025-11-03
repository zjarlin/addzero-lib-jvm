package site.addzero.util

/**
 * 函数盒子类
 *
 * @property restUrl 接口地址
 * @property methodType 请求方式
 * @property des 接口描述
 * @property funName 方法名称
 * @property paramiter 参数列表
 * @property returns 响应列表
 */
data class FunBox(
    /** 接口地址 */
    var restUrl: String? = null,

    /** 请求方式 */
    var methodType: String? = null,

    /** 接口描述 */
    var des: String? = null,

    /** 方法名称 */
    var funName: String ?= null,

    /** 参数列表 */
    var paramiter: MutableList<FieldDTO>? = null,

    /** 响应列表 */
    var returns: MutableList<FieldDTO> ?= null
)
