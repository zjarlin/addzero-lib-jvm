package site.addzero.easyexcel.find_merge_range

import cn.hutool.core.util.StrUtil
import java.lang.reflect.Field
import java.util.*


object MergeRangeFinder {


    fun <T> findMergeRanges(dataList: kotlin.collections.MutableList<T?>): kotlin.collections.MutableList<Range?> {
        val strings = MergeRangeFinder.data2Array<T?>(dataList)
        return MergeRangeFinder.findMergeRanges(strings)
    }


    /**
     * 默认一行表头修正区域行索引+1
     */
    fun findMergeRanges(dataArray: kotlin.Array<kotlin.Array<kotlin.String?>?>): kotlin.collections.MutableList<Range?> {
        return MergeRangeFinder.findMergeRanges(1, dataArray)
    }

    fun findMergeRanges(
        headerNo: kotlin.Int,
        dataArray: kotlin.Array<kotlin.Array<kotlin.String?>?>
    ): kotlin.collections.MutableList<Range?> {
        val mergeRanges: kotlin.collections.MutableList<Range?> = java.util.ArrayList<Range?>()

        val rows = dataArray.size
        val cols = dataArray[0]!!.size

        val visited = kotlin.Array<kotlin.BooleanArray?>(rows) { kotlin.BooleanArray(cols) }

        for (row in 0..<rows) {
            for (col in 0..<cols) {
                if (!visited[row]!![col]) {
                    val value = dataArray[row]!![col]
                    var endRow = row
                    var endCol = col

                    // Find the end of the range in the current row
                    while (endCol + 1 < cols && dataArray[row]!![endCol + 1] == value) {
                        endCol++
                    }

                    // Find the end of the range in the current column
                    while (endRow + 1 < rows && dataArray[endRow + 1]!![col] == value) {
                        endRow++
                    }

                    // Mark the cells as visited
                    for (i in row..endRow) {
                        for (j in col..endCol) {
                            visited[i]!![j] = true
                        }
                    }

                    val mergeType = MergeRangeFinder.getMergeType(endRow - row, endCol - col)
                    if (StrUtil.isNotBlank(mergeType)) {
                        mergeRanges.add(Range(row + headerNo, endRow + headerNo, col, endCol, mergeType))
                    }
                }
            }
        }

        return mergeRanges
    }

    private fun getMergeType(rowSpan: kotlin.Int, colSpan: kotlin.Int): kotlin.String? {
        if (rowSpan > 0 && colSpan > 0) {
            return "3"
        } else if (rowSpan > colSpan) {
            return "2" // 纵向合并优先
        } else if (colSpan > rowSpan) {
            return "1" // 横向合并优先
        } else {
            return null // 横纵合并range面积优先原则
        }
    }

    fun <T> data2Array(items: kotlin.collections.MutableList<T?>): kotlin.Array<kotlin.Array<kotlin.String?>?> {
        // 转为二维数组
        val dataArray: Array<Array<String?>?> = items.stream()
            .map {
                Arrays.stream<Field>(it!!.javaClass.getDeclaredFields())
                    .map<String?> { field: Field? ->
                        getColumnValue<T?>(
                            it,
                            field!!.getName()
                        )
                    }
                    .toArray()
            }
            .toArray() as Array<Array<String?>?>
        return dataArray
    }

    fun <T> getColumnValue(item: T?, fieldName: kotlin.String): kotlin.String? {
        try {
            val field = item!!.javaClass.getDeclaredField(fieldName)
            field.setAccessible(true)
            return field.get(item).toString()
        } catch (e: java.lang.NoSuchFieldException) {
            e.printStackTrace()
            return null
        } catch (e: java.lang.IllegalAccessException) {
            e.printStackTrace()
            return null
        }
    }
}
