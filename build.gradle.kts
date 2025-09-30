import java.text.SimpleDateFormat
import java.util.*
val versionDate: String? = SimpleDateFormat("yyyy.MM.dd").format(Date())
version = versionDate.toString()
// 或者使用setter方法
subprojects {
    if (path.startsWith(":lib:") && name.contains("addzero-")) {
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
