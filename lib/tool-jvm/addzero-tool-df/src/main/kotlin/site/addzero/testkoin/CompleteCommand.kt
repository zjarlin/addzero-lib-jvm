@file:OptIn(ExperimentalCli::class)

package site.addzero.testkoin

import NumberSelectableCliRepl
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

val commandService: CommandService by inject(CommandService::class.java)

@Single
class CommandService(
    val cmds: List<Subcommand>
)

private const val applicationName = "dotfiles-cli"

class CommandApp(val args: Array<String>) : KoinComponent {
    private val service: CommandService by inject()

    val cmds = service.cmds

    fun runApp() {
        if (args.isEmpty()) {
            startReplMode(cmds)
            return
        }
        val parser = ArgParser(applicationName)
        parser.subcommands(
            *cmds.toTypedArray()
        )
        parser.parse(args)
    }

    private fun startReplMode(cmds: List<Subcommand>) {
        NumberSelectableCliRepl(applicationName, cmds).start()
    }
}
