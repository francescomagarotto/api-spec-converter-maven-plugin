package io.github.apitools.maven;

public class ConversionOptions {
    private String source;
    private SpecFormat from;
    private SpecFormat to;
    private OutputSyntax syntax;
    private FieldOrder order;
    private boolean validate;
    private boolean fillMissing;

    private ConversionOptions() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConversionOptions options = new ConversionOptions();

        public Builder source(String source) {
            options.source = source;
            return this;
        }

        public Builder from(SpecFormat from) {
            options.from = from;
            return this;
        }

        public Builder to(SpecFormat to) {
            options.to = to;
            return this;
        }

        public Builder syntax(OutputSyntax syntax) {
            options.syntax = syntax;
            return this;
        }

        public Builder order(FieldOrder order) {
            options.order = order;
            return this;
        }

        public Builder validate(boolean validate) {
            options.validate = validate;
            return this;
        }

        public Builder fillMissing(boolean fillMissing) {
            options.fillMissing = fillMissing;
            return this;
        }

        public ConversionOptions build() {
            return options;
        }
    }

    // Getters
    public String getSource() { return source; }
    public SpecFormat getFrom() { return from; }
    public SpecFormat getTo() { return to; }
    public OutputSyntax getSyntax() { return syntax; }
    public FieldOrder getOrder() { return order; }
    public boolean isValidate() { return validate; }
    public boolean isFillMissing() { return fillMissing; }
}
