package site.addzero.example

import site.addzero.example.vendor.Text
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

@SpreadPackCarrierOf("site.addzero.example.vendor.Text")
class TextProps

@GenerateSpreadPackOverloads
fun printTextProps(
    @SpreadPack
    props: TextProps,
): String {
    return "TextProps[text,color,maxLines,softWrap,onTextLayout]=" +
        "(${props.text},${props.color},${props.maxLines},${props.softWrap},callback)"
}

@GenerateSpreadPackOverloads
fun MyText(
    @SpreadPack
    props: TextProps,
): String {
    return Text(
        text = "[MyText] ${props.text}",
        color = props.color,
        maxLines = props.maxLines,
        softWrap = props.softWrap,
        onTextLayout = props.onTextLayout,
    )
}

fun invokeSpreadPackExample(): String {
    val printedProps = printTextProps(
        text = "hello",
        color = "blue",
        maxLines = 2,
        softWrap = false,
        onTextLayout = { _ -> "layout" },
    )
    val wrappedText = MyText(
        text = "world",
        color = "red",
        maxLines = 3,
        softWrap = true,
        onTextLayout = { _ -> "wrapped-layout" },
    )
    return "$printedProps|$wrappedText"
}
