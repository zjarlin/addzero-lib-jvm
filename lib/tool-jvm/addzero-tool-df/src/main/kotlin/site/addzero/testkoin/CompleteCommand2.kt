//@file:OptIn(ExperimentalCli::class)
//
//package site.addzero.testkoin
//
//import kotlinx.cli.ArgParser
//import kotlinx.cli.ExperimentalCli
//import kotlinx.cli.Subcommand
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//
//
//class CommandApp2(val args: Array<String>) : KoinComponent {
//    val cmds: List<Subcommand> by inject()
//    fun runApp() {
////        if (args.isEmpty()) {
////            startReplMode()
////            return
////        }
//        val parser = ArgParser("dotfiles-cli")
//        parser.subcommands(
//            *cmds.toTypedArray()
//        )
//        parser.parse(args)
//    }
//}
