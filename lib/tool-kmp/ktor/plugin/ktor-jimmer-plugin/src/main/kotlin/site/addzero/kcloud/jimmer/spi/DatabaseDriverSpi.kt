package site.addzero.kcloud.jimmer.spi

import org.babyfish.jimmer.sql.dialect.Dialect
import site.addzero.kcloud.jimmer.di.DatasourceProperties
import javax.sql.DataSource

/**
 * 数据库驱动 SPI 接口。
 *
 * 每种数据库类型实现此接口，提供：
 * - 驱动匹配判断
 * - DataSource 创建
 * - Jimmer Dialect 提供
 * - Jimmer Dialect 提供
 *
 * 新增数据库类型只需新增实现类并标注 @Single，无需修改现有代码。
 */
interface DatabaseDriverSpi {

    /** 判断此驱动是否能处理给定的 driver 字符串 */
    fun supports(driver: String): Boolean

    /** 创建 DataSource */
    fun createDataSource(props: DatasourceProperties): DataSource

    /** 返回对应的 Jimmer Dialect */
    fun dialect(): Dialect
}
