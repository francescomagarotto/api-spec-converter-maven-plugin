package io.github.apitools.maven;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SpecFormat {
    SWAGGER_2("swagger_2"),
    OPENAPI_3("openapi_3");

    private final String value;

    SpecFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SpecFormat fromString(String value) {
        for (SpecFormat format : values()) {
            if (format.value.equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unrecognized format: " + value);
    }

    public static boolean isSupported(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getSupportedFormats() {
        return Arrays.stream(values())
                .map(SpecFormat::getValue)
                .collect(Collectors.joining(", "));
    }
}
