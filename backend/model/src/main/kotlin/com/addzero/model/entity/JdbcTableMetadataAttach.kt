package com.addzero.model.entity

import com.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table

/**
 * JdbcTableMetadataAttach
 *
 * 对应数据库表: jdbc_table_metadata_attach
 */
@Entity
@Table(name = "jdbc_table_metadata_attach")
interface JdbcTableMetadataAttach : BaseEntity {

    /**
     * jdbcTableMetadataId
     */
    @Column(name = "jdbc_table_metadata_id")
    val jdbcTableMetadataId: Long

    /**
     * showactions
     */
    @Column(name = "showactions")
    val showactions: Boolean

    /**
     * 行高
     */
    @Column(name = "rowheight")
    val rowheight: Long

}
