package io.github.apitools.maven.utils;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.github.apitools.maven.SpecFormat;

import java.util.ArrayList;
import java.util.List;

public class OpenApiValidator {

    public static List<String> validate(String content, SpecFormat format) {
        List<String> warnings = new ArrayList<>();

        try {
            if (format == SpecFormat.OPENAPI_3 || format == SpecFormat.SWAGGER_2) {
                ParseOptions options = new ParseOptions();
                options.setResolve(false);

                SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);

                if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                    warnings.addAll(result.getMessages());
                }
            }
        } catch (Exception e) {
            warnings.add("Error during validation: " + e.getMessage());
        }

        return warnings;
    }
}
