package site.addzero.example

import site.addzero.example.vendor.Text
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPackOf

@GenerateSpreadPackOverloads
fun printTextProps(
    @SpreadPackOf(
        "site.addzero.example.vendor.Text",
        exclude = ["onTextLayout"],
    )
    props: TextProps,
): String {
    return "TextProps[text,color,maxLines,softWrap,onTextLayout]=" +
        "(${props.text},${props.color},${props.maxLines},${props.softWrap},callback-fixed)"
}

@GenerateSpreadPackOverloads
fun MyText(
    @SpreadPackOf(
        "site.addzero.example.vendor.Text",
        exclude = ["onTextLayout"],
    )
    props: TextProps,
): String {
    return Text(
        text = "[MyText] ${props.text}",
        color = props.color,
        maxLines = props.maxLines,
        softWrap = props.softWrap,
        onTextLayout = { _ -> "wrapped-layout" },
    )
}

fun invokeSpreadPackExample(): String {
    val printedProps = printTextProps(
        text = "hello",
        color = "blue",
        maxLines = 2,
        softWrap = false,
    )
    val wrappedText = MyText(
        text = "world",
        color = "red",
        maxLines = 3,
        softWrap = true,
    )
    return "$printedProps|$wrappedText"
}
