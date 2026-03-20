package site.addzero.example.lib

object AutoWhereUtil {
    fun greet(name: String): String {
        return "Hello, $name"
    }
}

class CompanionHolder {
    companion object {
        fun join(left: String, right: String): String {
            return "$left-$right"
        }
    }
}
