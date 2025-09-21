package io.github.apitools.maven;

public enum FieldOrder {
    OPENAPI("openapi"),
    ALPHA("alpha");

    private final String value;

    FieldOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static FieldOrder fromString(String value) {
        for (FieldOrder order : values()) {
            if (order.value.equalsIgnoreCase(value)) {
                return order;
            }
        }
        throw new IllegalArgumentException("Unrecognized field order: " + value);
    }

    public static boolean isSupported(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
