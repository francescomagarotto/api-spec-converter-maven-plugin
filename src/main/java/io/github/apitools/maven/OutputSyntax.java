package io.github.apitools.maven;

public enum OutputSyntax {
    JSON("json"),
    YAML("yaml");

    private final String value;

    OutputSyntax(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static OutputSyntax fromString(String value) {
        for (OutputSyntax syntax : values()) {
            if (syntax.value.equalsIgnoreCase(value)) {
                return syntax;
            }
        }
        throw new IllegalArgumentException("Unrecognized syntax: " + value);
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
