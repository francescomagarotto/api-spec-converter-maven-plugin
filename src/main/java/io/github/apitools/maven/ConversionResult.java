package io.github.apitools.maven;

import java.util.Collections;
import java.util.List;

public record ConversionResult(String content, List<String> warnings) {

    public ConversionResult(String content, List<String> warnings) {
        this.content = content;
        this.warnings = warnings != null ? warnings : Collections.emptyList();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
