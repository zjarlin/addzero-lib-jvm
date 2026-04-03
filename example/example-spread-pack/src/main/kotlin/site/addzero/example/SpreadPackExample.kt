package site.addzero.example

import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadArgsOf
import site.addzero.kcp.spreadpack.SpreadOverload
import site.addzero.kcp.spreadpack.SpreadOverloadsOf
import site.addzero.kcp.spreadpack.SpreadPack
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
        overload = SpreadOverload(
            of = SpreadOverloadsOf("site.addzero.example.renderBase"),
            parameterTypes = [BaseOptions::class],
        ),
        exclude = ["debug"],
    )
    args: WrapperArgs,
): String {
    val done = args.onDone?.invoke() ?: "-"
    return "wrapper:${args.title}:${args.count}:$done"
}

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
    return "$formResult|$wrapperResult"
}
