package site.addzero.web.infra.jimmer.base.exhandler.autoaddcol.strategy

import site.addzero.entity.Res
import site.addzero.web.infra.jimmer.base.exhandler.autoaddcol.AutoAddColStrategy
import org.springframework.stereotype.Component

@Component
class DefaultAddColStrategy : AutoAddColStrategy {
    override fun canHandle(message: String?, causeMessage: String?): Boolean = true

    override fun handle(message: String?, causeMessage: String?): Any? {
        return Res.fail(causeMessage)
    }
}
