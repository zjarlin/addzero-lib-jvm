package site.addzero.easyexcel.entity

/**
 * 导出sheet配置
 *
 * @author zjarlin
 * @since 2023/11/15 10:06
 */
open class ExportSheetConfig {
    /**
     * 自适应列宽
     */
    private var adaptiveColumnWidth: Boolean? = null

    /**
     * 自适应行高
     */
    private var adaptiveRowHeight: Boolean? = null

    /**
     * 取消导出Excel的默认风格
     */
    private var useDefaultStyle: Boolean? = null

    /**
     * 自动清理空值
     */
    private var autoTrim: Boolean? = null

    /**
     * 自动合并表头
     */
    private var automaticMergeHead: Boolean? = null

    /**
     * 是否自动合并单元格
     */
    private var automaticallyMergeCells: Boolean? = null

    /**
     * 是否居中
     */
    private var isCenter: Boolean? = null

    private constructor(sheetName: String?, data: MutableList<*>?) {
        this.sheetName = sheetName
        this.data = data
    }

    var sheetName: String? = null
    var data: MutableList<*>? = null

    constructor() {
        //自适应列宽

        this.adaptiveColumnWidth = true
        // 自适应行高
        this.adaptiveRowHeight = true
        // 取消导出Excel的默认风格
        useDefaultStyle = false
        //自动清理空值
        autoTrim = true
        //自动合并表头
        automaticMergeHead = true
        //自动合并等值单元格
        automaticallyMergeCells = false
        //是否居中
        isCenter = true
    }

    companion object {
        fun createExportSheetConfig(sheetName: String?, data: MutableList<*>?): ExportSheetConfig {
            return ExportSheetConfig(sheetName, data)
        }
    }
}
