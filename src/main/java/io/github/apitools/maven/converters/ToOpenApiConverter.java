package io.github.apitools.maven.converters;

import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.maven.plugin.logging.Log;

public class ToOpenApiConverter {

    public ToOpenApiConverter(Log log) {
        // Constructor kept for compatibility
    }

    public OpenAPI convertToOpenAPI(ParsedSpec parsedSpec) {
        SpecFormat sourceFormat = parsedSpec.getFormat();

        // For both OpenAPI 3 and Swagger 2, the SwaggerParser already converts them to OpenAPI objects
        if (parsedSpec.getData() instanceof OpenAPI) {
            return (OpenAPI) parsedSpec.getData();
        }

        throw new IllegalArgumentException("Conversion not supported from: " + sourceFormat);
    }
}
