package site.addzero.biz.spec.iot

import java.util.LinkedHashMap

/**
 * Thing property definition supplied by the consumer application.
 */
class IotPropertySpec private constructor(
    val identifier: String,
    val name: String?,
    val description: String?,
    val unit: String?,
    val valueType: IotValueType,
    val length: Int?,
    val attributes: Map<String, String>,
) {

    class Builder {

        private var identifier: String? = null
        private var name: String? = null
        private var description: String? = null
        private var unit: String? = null
        private var valueType: IotValueType? = null
        private var length: Int? = null
        private val attributes = linkedMapOf<String, String>()

        fun identifier(identifier: String?) = apply {
            this.identifier = identifier
        }

        fun name(name: String?) = apply {
            this.name = name
        }

        fun description(description: String?) = apply {
            this.description = description
        }

        fun unit(unit: String?) = apply {
            this.unit = unit
        }

        fun valueType(valueType: IotValueType?) = apply {
            this.valueType = valueType
        }

        fun length(length: Int?) = apply {
            this.length = length
        }

        fun attribute(key: String?, value: String?) = apply {
            val cleanKey = requireText(key, "attribute key")
            val cleanValue = requireText(value, "attribute value")
            attributes[cleanKey] = cleanValue
        }

        fun build(): IotPropertySpec {
            return IotPropertySpec(
                identifier = requireText(identifier, "identifier"),
                name = trimToNull(name),
                description = trimToNull(description),
                unit = trimToNull(unit),
                valueType = valueType ?: throw IllegalArgumentException("valueType must not be null"),
                length = length,
                attributes = LinkedHashMap(attributes).toMap(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
