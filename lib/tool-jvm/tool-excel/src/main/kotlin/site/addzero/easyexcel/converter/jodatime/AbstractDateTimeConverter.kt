package site.addzero.easyexcel.converter.jodatime

import cn.idev.excel.converters.Converter
import cn.idev.excel.metadata.GlobalConfiguration
import cn.idev.excel.metadata.data.ReadCellData
import cn.idev.excel.metadata.data.WriteCellData
import cn.idev.excel.metadata.property.ExcelContentProperty
import java.lang.reflect.ParameterizedType
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

abstract class AbstractDateTimeConverter<T> : Converter<T?> {
    private val type: Class<T?>

    init {
        this.type = (javaClass.getGenericSuperclass() as ParameterizedType).getActualTypeArguments()[0] as Class<T?>
    }

    protected abstract val formatter: DateTimeFormatter?

    public override fun supportJavaTypeKey(): Class<T?> {
        return type
    }

    @Throws(Exception::class)
    public override fun convertToExcelData(
        value: T?,
        contentProperty: ExcelContentProperty?,
        globalConfiguration: GlobalConfiguration?
    ): WriteCellData<*>? {
        return WriteCellData<T?>(this.formatter?.format(value as TemporalAccessor?))
    }

    public override fun convertToJavaData(
        cellData: ReadCellData<*>,
        contentProperty: ExcelContentProperty?,
        globalConfiguration: GlobalConfiguration?
    ): T? {
        return parse(cellData.getStringValue())
    }

    protected abstract fun parse(value: String?): T?
}
