package site.addzero.network.call.tianyancha.utils


object EmUtils {
    fun removeEmTag(value: String?): String? {
        var value = value
        if (value != null) {
            value = value.replace("<em>", "").replace("</em>", "")
        }
        return value
    }
}
