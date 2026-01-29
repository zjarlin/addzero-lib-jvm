package site.addzero.email

import cn.hutool.poi.excel.ExcelUtil
import cn.hutool.poi.excel.ExcelWriter
import org.junit.jupiter.api.Test
import java.io.File

class ExcelTest {
  @Test
  fun `filter excel data and write to new workbook`() {
    val inputFile = File("/Users/zjarlin/Desktop/工作簿1.xlsx")
    val outputFile = File("/Users/zjarlin/Desktop/工作簿2.xlsx")

    val allData: List<Map<String, Any>> = ExcelUtil.getReader(inputFile, 0).use { reader ->
      // Read data as List<Map<String, Any>>, using the first row (index 0) as headers.
      reader.readAll()
    }

    val stationNameMap = mapOf(
      "山河" to "洛阳山河混凝土科技工程有限公司",
      "创世" to "洛阳创世建材有限公司",
      "天骄" to "洛阳天骄混凝土有限公司",
      "正业" to "洛阳正业新型建材有限公司",
      "信大" to "河南信大混凝土有限公司",
      "元洲" to "洛阳元洲混凝土有限公司",
    )

    val filteredData = allData.filter { row ->
      val bool = row["试块类型"] == "标养"
      val any = row["制作日期"]
      val notBlank = any.toString().isNotBlank()
      bool && notBlank
    }.map {
      val updated = LinkedHashMap<String, Any>()
      updated.putAll(it)
      val station = it["商混站"]?.toString()?.trim()
      val fullName = if (station.isNullOrEmpty()) null else stationNameMap[station]
      if (fullName != null) {
        updated["商混站"] = fullName
      }
      updated
    }

    ExcelUtil.getWriter(outputFile).use { writer ->
      // Write the filtered data to the new Excel file, including headers.
      writer.write(filteredData, true)
      writer.autoSizeColumnAll()
    }

    println("Filtered data successfully written to: ${outputFile.absolutePath}")
  }
}
