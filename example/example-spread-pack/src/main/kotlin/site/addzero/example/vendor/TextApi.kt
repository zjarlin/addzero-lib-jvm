package site.addzero.example.vendor

fun Text(
    text: String,
    color: String,
    maxLines: Int,
    softWrap: Boolean,
    onTextLayout: (String) -> String,
): String {
    val layout = onTextLayout(text)
    return "Text(text=$text,color=$color,maxLines=$maxLines,softWrap=$softWrap,layout=$layout)"
}
