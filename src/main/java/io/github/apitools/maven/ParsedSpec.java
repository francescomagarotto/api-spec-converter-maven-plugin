package io.github.apitools.maven;

public class ParsedSpec {
    private final SpecFormat format;
    private final Object data;

    public ParsedSpec(SpecFormat format, Object data) {
        this.format = format;
        this.data = data;
    }

    public SpecFormat getFormat() {
        return format;
    }

    public Object getData() {
        return data;
    }
}
