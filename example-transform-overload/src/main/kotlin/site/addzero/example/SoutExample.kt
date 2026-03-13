package site.addzero.example

import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads

@GenerateTransformOverloads
interface SoutExample {
    fun sout(
        t: T,
        r: R,
    ): String
}
