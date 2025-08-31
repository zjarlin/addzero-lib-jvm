package com.addzero.model.entity

import com.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table

/**
 * 区域表
 *
 * 对应数据库表: sys_area
 */
@Entity
@Table(name = "sys_area")
interface SysArea : BaseEntity {

    /**
     * 上级
     */
    @Column(name = "parent_id")
    val parentId: Long?

    /**
     * 1省,2市,3区
     */
    @Column(name = "node_type")
    val nodeType: String?

    /**
     * name
     */
    @Column(name = "name")
    val name: String?

    /**
     * 区域编码
     */
    @Column(name = "area_code")
    val areaCode: String?
}
