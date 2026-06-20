package site.addzero.common.util.data_structure.spo.util

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil

object SpoFileReader {
    fun readTab(filePath: String?): List<Map<String, String>> {
        val lines = FileUtil.readUtf8Lines(filePath)
        return readTabByLines(lines)
    }

    private fun readTabByLines(lines: List<String>): List<Map<String, String>> {
        if (lines.isEmpty()) {
            return emptyList()
        }

        val headers = lines[0].split("\t").map { it.trim() }

        return lines.asSequence()
            .drop(1)
            .filter { StrUtil.isNotBlank(it) }
            .map { line -> line.split("\t").map { it.trim() } }
            .map { fields -> mapToMap(fields, headers) }
            .toList()
    }

    private fun mapToMap(fields: List<String>, headers: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in fields.indices) {
            if (i < headers.size) {
                map[headers[i]] = fields[i]
            }
        }
        return map
    }
}
