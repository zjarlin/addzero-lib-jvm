package com.addzero.model.entity

import com.addzero.entity2form.annotation.FormIgnore
import com.addzero.model.common.SnowflakeIdGenerator
import org.babyfish.jimmer.sql.*

/**
 * 追加的列元数据
 *
 * 对应数据库表: jdbc_column_metadata_attach
 */
@Entity
@Table(name = "jdbc_column_metadata_attach")
interface JdbcColumnMetadataAttach {

    @Id
    @GeneratedValue(generatorType = SnowflakeIdGenerator::class)
    val id: Long

    /**
     * 显示在列表
     */
    @Column(name = "show_in_list_flag")
    val showInListFlag: Boolean

    /**
     * 显示在表单
     */
    @Column(name = "show_in_form_flag")
    val showInFormFlag: Boolean

    /**
     * 显示在搜索
     */
    @Column(name = "show_in_search_flag")
    val showInSearchFlag: Boolean


    @OneToOne
    @JoinColumn(name = "column_id")
    @OnDissociate(DissociateAction.DELETE)
    @FormIgnore
    val jdbcColumnMetadata: JdbcColumnMetadata?
}
