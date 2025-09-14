package site.addzero.web.infra.jimmer.base.exhandler.autoaddcol.strategy

import site.addzero.common.util.metainfo.MetaInfoUtils.extractTableName
import site.addzero.entity.Res
import site.addzero.web.infra.jimmer.base.exhandler.autoaddcol.AutoAddColStrategy
import site.addzero.web.infra.jimmer.base.exhandler.extractColumnNames
import site.addzero.web.infra.jimmer.base.exhandler.fixCol
import org.springframework.stereotype.Component

@Component
class MessageDoesNotExistStrategy : AutoAddColStrategy {
    override fun canHandle(message: String?, causeMessage: String?): Boolean? {
        return message?.contains("does not exist")
    }

    override fun handle(message: String?, causeMessage: String?): Any? {
        val tableName = extractTableName(message)
        val columnName = extractColumnNames(causeMessage)
        val fixCol = fixCol(tableName, columnName)
        return Res.success(fixCol)
    }
}
