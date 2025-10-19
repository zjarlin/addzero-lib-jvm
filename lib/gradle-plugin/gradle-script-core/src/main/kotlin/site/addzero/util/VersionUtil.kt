package site.addzero.util

import java.text.SimpleDateFormat
import java.util.*

object VersionUtil {
    // 解析当前版本并生成下一个版本
    fun nextVersion(currentVersion: String?): String {

        val versionDate: String = defaultVersion()

        if (currentVersion == null) {
            return versionDate
        }

        // 如果当前版本是日期格式 (yyyy.MM.dd)
        val dateRegex = Regex("""^\d{4}\.\d{1,2}\.\d{1,2}$""")
        if (currentVersion.matches(dateRegex)) {
            // 如果当前版本日期等于今天的日期，则添加 .1 后缀
            if (currentVersion == versionDate) {
                return "$versionDate.1"
            }
            // 如果当前版本日期早于今天的日期，则使用今天的日期
            else {
                return versionDate
            }
        }

        // 如果当前版本是日期+数字格式 (yyyy.MM.dd.n)
        val dateWithNumberRegex = Regex("""^(\d{4}\.\d{1,2}\.\d{1,2})\.(\d+)$""")
        val matchResult = dateWithNumberRegex.find(currentVersion)
        if (matchResult != null) {
            val datePart = matchResult.groupValues[1]
            val numberPart = matchResult.groupValues[2].toInt()

            // 如果日期部分等于今天，则递增数字部分
            if (datePart == versionDate) {
                return "$versionDate.${numberPart + 1}"
            }
            // 如果日期部分早于今天，则使用今天的日期
            else {
                return versionDate
            }
        }

        // 其他情况，直接使用今天的日期
        return versionDate
    }

    fun defaultVersion(): String {
        val versionDate: String = SimpleDateFormat("yyyy.MM.dd").format(Date())
        return versionDate
    }

}
