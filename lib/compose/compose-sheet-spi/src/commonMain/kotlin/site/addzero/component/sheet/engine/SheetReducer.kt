package site.addzero.component.sheet.engine

/**
 * 纯函数式文档 reducer。
 *
 * 它既可用于本地 optimistic update，也可用于服务端回放操作日志。
 */
object SheetReducer {
    fun apply(
        document: SheetDocument,
        operations: List<SheetOperation>,
    ): SheetDocument {
        return operations.fold(document) { current, operation ->
            apply(current, operation)
        }
    }

    fun apply(
        document: SheetDocument,
        operation: SheetOperation,
    ): SheetDocument {
        return when (operation) {
            is PutCellValue -> updateSheet(document, operation.sheetId) { page ->
                page.copy(
                    cells = page.cells + (operation.address to operation.value),
                )
            }

            is ClearRange -> updateSheet(document, operation.sheetId) { page ->
                page.copy(
                    cells = page.cells.filterKeys { address ->
                        !operation.range.contains(address)
                    },
                )
            }

            is InsertRows -> updateSheet(document, operation.sheetId) { page ->
                val nextCells = buildMap {
                    page.cells.forEach { (address, value) ->
                        val nextAddress = if (address.rowIndex >= operation.startRowIndex) {
                            address.copy(rowIndex = address.rowIndex + operation.count)
                        } else {
                            address
                        }
                        put(nextAddress, value)
                    }
                }
                page.copy(
                    rowCount = page.rowCount + operation.count,
                    cells = nextCells,
                )
            }

            is DeleteRows -> updateSheet(document, operation.sheetId) { page ->
                val nextCells = buildMap {
                    page.cells.forEach { (address, value) ->
                        when {
                            address.rowIndex < operation.startRowIndex -> {
                                put(address, value)
                            }

                            address.rowIndex >= operation.startRowIndex + operation.count -> {
                                put(
                                    address.copy(rowIndex = address.rowIndex - operation.count),
                                    value,
                                )
                            }
                        }
                    }
                }
                page.copy(
                    rowCount = maxOf(1, page.rowCount - operation.count),
                    cells = nextCells,
                )
            }

            is InsertColumns -> updateSheet(document, operation.sheetId) { page ->
                val nextCells = buildMap {
                    page.cells.forEach { (address, value) ->
                        val nextAddress = if (address.columnIndex >= operation.startColumnIndex) {
                            address.copy(columnIndex = address.columnIndex + operation.count)
                        } else {
                            address
                        }
                        put(nextAddress, value)
                    }
                }
                page.copy(
                    columnCount = page.columnCount + operation.count,
                    cells = nextCells,
                )
            }

            is DeleteColumns -> updateSheet(document, operation.sheetId) { page ->
                val nextCells = buildMap {
                    page.cells.forEach { (address, value) ->
                        when {
                            address.columnIndex < operation.startColumnIndex -> {
                                put(address, value)
                            }

                            address.columnIndex >= operation.startColumnIndex + operation.count -> {
                                put(
                                    address.copy(columnIndex = address.columnIndex - operation.count),
                                    value,
                                )
                            }
                        }
                    }
                }
                page.copy(
                    columnCount = maxOf(1, page.columnCount - operation.count),
                    cells = nextCells,
                )
            }

            is FillDownRange -> updateSheet(document, operation.sheetId) { page ->
                val normalizedStart = operation.range.normalizedStart
                val normalizedEnd = operation.range.normalizedEnd
                val nextCells = page.cells.toMutableMap()

                for (columnIndex in normalizedStart.columnIndex..normalizedEnd.columnIndex) {
                    val sourceAddress = SheetCellAddress(
                        rowIndex = normalizedStart.rowIndex,
                        columnIndex = columnIndex,
                    )
                    val sourceValue = page.cell(sourceAddress)
                    for (rowIndex in (normalizedStart.rowIndex + 1)..normalizedEnd.rowIndex) {
                        val targetAddress = SheetCellAddress(
                            rowIndex = rowIndex,
                            columnIndex = columnIndex,
                        )
                        if (sourceValue == null) {
                            nextCells.remove(targetAddress)
                        } else {
                            nextCells[targetAddress] = sourceValue
                        }
                    }
                }

                page.copy(cells = nextCells)
            }

            is FillRightRange -> updateSheet(document, operation.sheetId) { page ->
                val normalizedStart = operation.range.normalizedStart
                val normalizedEnd = operation.range.normalizedEnd
                val nextCells = page.cells.toMutableMap()

                for (rowIndex in normalizedStart.rowIndex..normalizedEnd.rowIndex) {
                    val sourceAddress = SheetCellAddress(
                        rowIndex = rowIndex,
                        columnIndex = normalizedStart.columnIndex,
                    )
                    val sourceValue = page.cell(sourceAddress)
                    for (columnIndex in (normalizedStart.columnIndex + 1)..normalizedEnd.columnIndex) {
                        val targetAddress = SheetCellAddress(
                            rowIndex = rowIndex,
                            columnIndex = columnIndex,
                        )
                        if (sourceValue == null) {
                            nextCells.remove(targetAddress)
                        } else {
                            nextCells[targetAddress] = sourceValue
                        }
                    }
                }

                page.copy(cells = nextCells)
            }

            is PasteCells -> updateSheet(document, operation.sheetId) { page ->
                val nextCells = page.cells.toMutableMap()
                var maxRowIndex = page.rowCount - 1
                var maxColumnIndex = page.columnCount - 1

                operation.patches.forEach { patch ->
                    val targetAddress = SheetCellAddress(
                        rowIndex = operation.startAddress.rowIndex + patch.rowOffset,
                        columnIndex = operation.startAddress.columnIndex + patch.columnOffset,
                    )
                    maxRowIndex = maxOf(maxRowIndex, targetAddress.rowIndex)
                    maxColumnIndex = maxOf(maxColumnIndex, targetAddress.columnIndex)

                    if (patch.value == null || patch.value.kind == SheetCellValueKind.EMPTY) {
                        nextCells.remove(targetAddress)
                    } else {
                        nextCells[targetAddress] = patch.value
                    }
                }

                page.copy(
                    rowCount = maxOf(1, maxRowIndex + 1),
                    columnCount = maxOf(1, maxColumnIndex + 1),
                    cells = nextCells,
                )
            }

            is RenameSheet -> updateSheet(document, operation.sheetId) { page ->
                page.copy(title = operation.title)
            }
        }.bumpVersion()
    }

    private fun updateSheet(
        document: SheetDocument,
        sheetId: String,
        transform: (SheetPage) -> SheetPage,
    ): SheetDocument {
        return document.copy(
            sheets = document.sheets.map { sheet ->
                if (sheet.sheetId == sheetId) {
                    transform(sheet)
                } else {
                    sheet
                }
            },
        )
    }

    private fun SheetDocument.bumpVersion(): SheetDocument {
        return copy(version = version + 1)
    }
}
