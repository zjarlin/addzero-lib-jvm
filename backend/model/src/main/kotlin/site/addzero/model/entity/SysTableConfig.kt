package site.addzero.model.entity

import site.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.entity2form.annotation.LabelProp

/**
 * SysTableConfig
 *
 * 对应数据库表: sys_table_config
 */
@Entity
@Table(name = "sys_table_config")
interface SysTableConfig : BaseEntity {
    /**
     * 路由键
     */
    @Column(name = "route_key")
    @LabelProp
    val routeKey: String

    /**
     * 显示分页控件
     */
    @Column(name = "show_pagination")
    val showPagination: Boolean

    /**
     * 显示搜索栏
     */
    @Column(name = "show_search_bar")
    val showSearchBar: Boolean

    /**
     * 显示批量操作
     */
    @Column(name = "show_batch_actions")
    val showBatchActions: Boolean

    /**
     * 显示每行的选择
     */
    @Column(name = "show_row_selection")
    val showRowSelection: Boolean

    /**
     * 显示默认行操作
     */
    @Column(name = "show_default_row_actions")
    val showDefaultRowActions: Boolean

    /**
     * 对该字段启用排序
     */
    @Column(name = "enable_sorting")
    val enableSorting: Boolean

    /**
     * 对该字段启用高级搜索
     */
    @Column(name = "enable_advanced_search")
    val enableAdvancedSearch: Boolean

    /**
     * 表头高度
     */
    @Column(name = "header_height_dp")
    val headerHeightDp: Float

    /**
     * 行高
     */
    @Column(name = "row_height_dp")
    val rowHeightDp: Float

    @OneToMany(mappedBy = "tableConfig")
    val columns: List<SysColumnConfig>
}
