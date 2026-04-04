package site.addzero.example

import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadArgsOf
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf
import site.addzero.kcp.spreadpack.SpreadPackSelector

data class FormOptions(
    val name: String,
    val enabled: Boolean,
    val onDone: (() -> String)? = null,
)

@GenerateSpreadPackOverloads
fun submitForm(
    @SpreadPack(selector = SpreadPackSelector.ATTRS)
    options: FormOptions,
): String {
    val done = options.onDone?.invoke() ?: "-"
    return "form:${options.name}:${options.enabled}:$done"
}

data class BaseOptions(
    val title: String = "",
    val count: Int = 0,
    val debug: Boolean = false,
    val onDone: (() -> String)? = null,
)

fun renderBase(
    @SpreadPack
    options: BaseOptions,
): String {
    val done = options.onDone?.invoke() ?: "-"
    return "base:${options.title}:${options.count}:${options.debug}:$done"
}

fun renderBase(title: String): String = "shadow:$title"

data class WrapperArgs(
    val title: String,
    val count: Int,
    val onDone: (() -> String)?,
)

@GenerateSpreadPackOverloads
fun renderWrapper(
    @SpreadPack
    @SpreadArgsOf(
        functionFqName = "site.addzero.example.renderBase",
        parameterTypes = [BaseOptions::class],
        exclude = ["debug"],
    )
    args: WrapperArgs,
): String {
    val done = args.onDone?.invoke() ?: "-"
    return "wrapper:${args.title}:${args.count}:$done"
}

@SpreadPackCarrierOf(
    functionFqName = "site.addzero.example.renderBase",
    parameterTypes = [BaseOptions::class],
    exclude = ["debug", "onDone"],
)
class RenderAliasArgs

@GenerateSpreadPackOverloads
fun renderAlias(
    @SpreadPack
    args: RenderAliasArgs,
): String = "alias:${args.title}:${args.count}:true"

fun invokeSpreadPackExample(): String {
    val formResult = submitForm(
        name = "demo",
        enabled = true,
    )
    val wrapperResult = renderWrapper(
        title = "hello",
        count = 2,
        onDone = { "done" },
    )
    val aliasResult = renderAlias(
        count = 3,
    )
    return "$formResult|$wrapperResult|$aliasResult"
}
