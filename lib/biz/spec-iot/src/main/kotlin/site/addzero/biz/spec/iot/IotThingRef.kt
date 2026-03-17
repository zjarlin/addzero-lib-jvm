package site.addzero.biz.spec.iot

/**
 * Generic thing reference used by schema and telemetry APIs.
 */
class IotThingRef private constructor(
    val kind: String,
    val id: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is IotThingRef) {
            return false
        }
        return kind == other.kind && id == other.id
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "$kind:$id"
    }

    companion object {
        @JvmStatic
        fun of(kind: String?, id: String?): IotThingRef {
            return IotThingRef(
                kind = requireText(kind, "kind"),
                id = requireText(id, "id"),
            )
        }
    }
}
