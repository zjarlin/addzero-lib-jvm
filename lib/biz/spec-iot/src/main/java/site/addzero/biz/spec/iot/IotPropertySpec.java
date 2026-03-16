package site.addzero.biz.spec.iot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thing property definition supplied by the consumer application.
 */
public final class IotPropertySpec {

    private final String identifier;
    private final String name;
    private final String description;
    private final String unit;
    private final IotValueType valueType;
    private final Integer length;
    private final Map<String, String> attributes;

    private IotPropertySpec(Builder builder) {
        this.identifier = requireText(builder.identifier, "identifier");
        this.name = trimToNull(builder.name);
        this.description = trimToNull(builder.description);
        this.unit = trimToNull(builder.unit);
        if (builder.valueType == null) {
            throw new IllegalArgumentException("valueType must not be null");
        }
        this.valueType = builder.valueType;
        this.length = builder.length;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, String>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public IotValueType getValueType() {
        return valueType;
    }

    public Integer getLength() {
        return length;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static final class Builder {

        private String identifier;
        private String name;
        private String description;
        private String unit;
        private IotValueType valueType;
        private Integer length;
        private final Map<String, String> attributes = new LinkedHashMap<String, String>();

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder valueType(IotValueType valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder length(Integer length) {
            this.length = length;
            return this;
        }

        public Builder attribute(String key, String value) {
            String cleanKey = requireText(key, "attribute key");
            String cleanValue = requireText(value, "attribute value");
            this.attributes.put(cleanKey, cleanValue);
            return this;
        }

        public IotPropertySpec build() {
            return new IotPropertySpec(this);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
