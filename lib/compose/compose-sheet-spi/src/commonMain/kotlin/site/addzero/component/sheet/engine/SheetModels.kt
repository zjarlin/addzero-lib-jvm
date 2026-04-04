package site.addzero.component.sheet.engine

/**
 * 单元格坐标。
 */
data class SheetCellAddress(
    val rowIndex: Int,
    val columnIndex: Int,
) {
    init {
        require(rowIndex >= 0) { "rowIndex 不能小于 0" }
        require(columnIndex >= 0) { "columnIndex 不能小于 0" }
    }

    val a1Notation
        get() = "${sheetColumnName(columnIndex)}${rowIndex + 1}"

    companion object {
        fun fromA1(value: String): SheetCellAddress? {
            val normalized = value.trim().uppercase()
            if (normalized.isBlank()) {
                return null
            }

            val letters = normalized.takeWhile { it.isLetter() }
            val digits = normalized.dropWhile { it.isLetter() }
            if (letters.isBlank() || digits.isBlank()) {
                return null
            }

            val row = digits.toIntOrNull()?.minus(1) ?: return null
            if (row < 0) {
                return null
            }

            return SheetCellAddress(
                rowIndex = row,
                columnIndex = sheetColumnIndex(letters),
            )
        }
    }
}

/**
 * 单元格值类型。
 */
enum class SheetCellValueKind {
    EMPTY,
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE_TIME,
    JSON,
    FORMULA,
}

/**
 * 单元格值。
 */
data class SheetCellValue(
    val kind: SheetCellValueKind = SheetCellValueKind.TEXT,
    val raw: String = "",
    val display: String = raw,
    val formula: String? = null,
    val formatKey: String? = null,
    val note: String? = null,
) {
    companion object {
        fun infer(raw: String): SheetCellValue {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) {
                return SheetCellValue(
                    kind = SheetCellValueKind.EMPTY,
                    raw = "",
                    display = "",
                )
            }

            return when {
                trimmed.startsWith("=") -> {
                    SheetCellValue(
                        kind = SheetCellValueKind.FORMULA,
                        raw = raw,
                        display = raw,
                        formula = trimmed,
                    )
                }

                trimmed.equals("true", ignoreCase = true) ||
                    trimmed.equals("false", ignoreCase = true) -> {
                    SheetCellValue(
                        kind = SheetCellValueKind.BOOLEAN,
                        raw = raw,
                        display = trimmed.lowercase(),
                    )
                }

                trimmed.toDoubleOrNull() != null -> {
                    SheetCellValue(
                        kind = SheetCellValueKind.NUMBER,
                        raw = raw,
                        display = raw,
                    )
                }

                else -> {
                    SheetCellValue(
                        kind = SheetCellValueKind.TEXT,
                        raw = raw,
                        display = raw,
                    )
                }
            }
        }
    }
}

/**
 * 视口状态。
 */
data class SheetViewport(
    val rowOffset: Int = 0,
    val columnOffset: Int = 0,
    val visibleRowCount: Int = 40,
    val visibleColumnCount: Int = 20,
    val zoomPercent: Int = 100,
) {
    init {
        require(rowOffset >= 0) { "rowOffset 不能小于 0" }
        require(columnOffset >= 0) { "columnOffset 不能小于 0" }
        require(visibleRowCount > 0) { "visibleRowCount 必须大于 0" }
        require(visibleColumnCount > 0) { "visibleColumnCount 必须大于 0" }
        require(zoomPercent in 25..400) { "zoomPercent 必须在 25..400" }
    }
}

fun sheetColumnName(columnIndex: Int): String {
    require(columnIndex >= 0) { "columnIndex 不能小于 0" }

    var value = columnIndex
    val builder = StringBuilder()
    do {
        val remainder = value % 26
        builder.append(('A'.code + remainder).toChar())
        value = value / 26 - 1
    } while (value >= 0)
    return builder.reverse().toString()
}

fun sheetColumnIndex(columnName: String): Int {
    require(columnName.isNotBlank()) { "columnName 不能为空" }

    var result = 0
    for (char in columnName.uppercase()) {
        require(char in 'A'..'Z') { "columnName 只能包含英文字母" }
        result = result * 26 + (char.code - 'A'.code + 1)
    }
    return result - 1
}
