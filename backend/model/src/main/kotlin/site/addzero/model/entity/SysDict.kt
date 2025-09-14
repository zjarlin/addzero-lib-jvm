package site.addzero.model.entity

import site.addzero.entity2form.annotation.LabelProp
import site.addzero.model.common.BaseDeletedEntity
import site.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.*

/**
 *字典
 * @author zjarlin
 * @date 2024/11/27
 * @constructor 创建[SysDict]
 */
@Entity
@Table(name = "sys_dict")
interface SysDict : BaseEntity, BaseDeletedEntity {

    /**
     *  字典名称
     */
    @LabelProp
    @Column(name = "dict_name")
    val dictName: String

    /**
     *  字典编码
     */
    @Column(name = "dict_code")
    @Key(group = "dictCode")
    val dictCode: String

    /**
     *  描述
     */
    @Key(group = "description")
    @LabelProp
    val description: String?


    @OneToMany(mappedBy = "sysDict")
    val sysDictItems: List<SysDictItem>


}
