package com.addzero.model.entity

import com.addzero.generated.enums.EnumShellPlatforms
import com.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.Default
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table


/**
 * <p>
 *  环境变量管理

 * </p>
 *
 * @author zjarlin
 * @date 2024-10-20
 */
@Entity
@Table(name = "biz_dotfiles")
interface BizDotfiles : BaseEntity {


    /**
     *  操作系统
     *  win=win
     * linux=linux
     * mac=mac
     *null=不限
     */

//    @Serialized
    // 临时注释掉，避免 KSP 处理顺序问题
    // val osType: List<EnumOsType>
    val osType: List<String>


//    @get:Schema(description = "操作系统")
//    @ManyToMany
//    @JoinTable(
//        name = "biz_mapping",
//        joinColumnName = "from_id",
//        inverseJoinColumnName = "to_id",
//        filter = JoinTableFilter(
//            columnName = "mapping_type",
//            values = ["dotfiles_tag_mapping"]
//        )
//    )
//    val osType: List<BizTag>


    /**
     *  系统架构
     *  arm64=arm64
     *  x86=x86
     *  不限=不限
     */
    @Key
    val osStructure: EnumShellPlatforms?

    /**
     *  定义类型
     *  alias=alias
     *  export=export
     * function=function
     * sh=sh
     * var=var
     */
    @Key
    // 临时使用字符串类型，避免 KSP 处理顺序问题
    // val defType: EnumShellDefType
    val defType: String

    /**
     *  名称
     */
    @Key
    val name: String

    /**
     *  值
     */
    val value: String

    /**
     *  注释
     */
    val describtion: String?

    /**
     *  状态
     *  1= 启用
     *  0= 未启用
     */
    @Key
    @Default("1")
    // 临时使用字符串类型，避免 KSP 处理顺序问题
    // val status: EnumSysToggle
    val status: String

    /** 文件地址 */
    val fileUrl: String?


    /** 文件位置 */
    val location: String?

}
