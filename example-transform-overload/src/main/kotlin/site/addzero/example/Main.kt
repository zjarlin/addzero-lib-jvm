package site.addzero.example

fun main() {
    val example: SoutExample = object : SoutExample {
        override fun sout(
            t: T,
            r: R,
        ): String {
            return "sout(t=${t.value}, r=${r.value})"
        }
    }
    println(example.sout(T("base"), R(1)))
    println(example.sout(S("from-s"), R(2)))
    println(example.sout(T("base"), G(3)))
    println(example.sout(S("from-s"), G(4)))
  val filter = SoutExample::class.java.methods
    .filter { method -> method.name == "sout" && method.parameterCount == 2 }
  filter
        .map { method ->
            method.parameterTypes.joinToString(
                prefix = "sout(",
                postfix = ")",
            ) { parameterType -> parameterType.simpleName }
        }
        .sorted()
        .forEach(::println)
}
