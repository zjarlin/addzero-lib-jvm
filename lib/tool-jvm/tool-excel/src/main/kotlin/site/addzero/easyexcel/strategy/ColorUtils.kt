package site.addzero.easyexcel.strategy

import org.apache.poi.ss.usermodel.IndexedColors
import kotlin.math.pow
import kotlin.math.sqrt

object ColorUtils {
    fun findClosestColor(red: Int, green: Int, blue: Int): IndexedColors? {
        var minDistance = Double.Companion.MAX_VALUE
        var closestColor: IndexedColors? = null

        for (color in IndexedColors.entries) {
            val colorRed = (color.index.toInt() shr 16) and 0xFF
            val colorGreen = (color.index.toInt() shr 8) and 0xFF
            val colorBlue = color.index.toInt() and 0xFF

            val distance = sqrt(
                (red - colorRed).toDouble().pow(2.0) + (green - colorGreen).toDouble()
                    .pow(2.0) + (blue - colorBlue).toDouble().pow(2.0)
            )

            if (distance < minDistance) {
                minDistance = distance
                closestColor = color
            }
        }

        return closestColor
    }
}
