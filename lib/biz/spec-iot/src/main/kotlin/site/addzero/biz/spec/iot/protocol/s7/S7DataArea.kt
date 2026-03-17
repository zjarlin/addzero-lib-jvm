package site.addzero.biz.spec.iot.protocol.s7

/**
 * Common S7 data areas.
 */
enum class S7DataArea(
    val code: String,
) {
    DB("DB"),
    I("I"),
    Q("Q"),
    M("M"),
    V("V"),
}
