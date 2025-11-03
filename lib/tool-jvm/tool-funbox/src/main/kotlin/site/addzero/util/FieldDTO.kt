package site.addzero.util

import io.swagger.annotations.ApiModelProperty

/**
 * 字段信息DTO类
 *
 * @property restName 接口名称
 * @property restUrl 接口地址
 * @property modelName 模块描述
 * @property fieldName 字段中文名
 * @property fieldEng 字段英文名
 * @property fieldType 字段类型
 * @property fieldLong 字段长度
 * @author addzero
 */
data class FieldDTO(
    /** 接口名称 */
    @ApiModelProperty("接口名称")
//    @get:ApiModelProperty("接口名称")
//    @set:ApiModelProperty("接口名称")
//    @field:ApiModelProperty("接口名称")
    var restName: String? = null,

    /** 接口地址 */
    var restUrl: String? = null,

    /** 模块描述 */
    var modelName: String? = null,

    /** 字段中文名 */
    var fieldName: String? = null,

    /** 字段英文名 */
    var fieldEng: String? = null,

    /** 字段类型 */
    var fieldType: String? = null,

    /** 字段长度 */
    var fieldLong: String? = null
)


class FieldDTO2 {
    /** 接口名称 */
    @get:ApiModelProperty("接口名称")
    var restName: String? = null

    /** 接口地址 */
    var restUrl: String? = null

    /** 模块描述 */
    var modelName: String? = null

    /** 字段中文名 */
    var fieldName: String? = null

    /** 字段英文名 */
    var fieldEng: String? = null

    /** 字段类型 */
    var fieldType: String? = null

    /** 字段长度 */
    var fieldLong: String? = null
}
