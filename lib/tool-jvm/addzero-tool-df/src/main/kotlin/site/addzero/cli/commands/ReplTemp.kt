package site.addzero.cli.commands

import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl

@Single
class ReplTemp(val repls: List<AdvancedRepl<*, *>>) {

}
