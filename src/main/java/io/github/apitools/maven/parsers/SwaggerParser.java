package io.github.apitools.maven.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

public class SwaggerParser {

    private final Log log;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public SwaggerParser(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();
    }

    public ParsedSpec parse(String content, SpecFormat format) throws Exception {
        return switch (format) {
            case SWAGGER_2 -> parseSwagger2(content);
            case OPENAPI_3 -> parseOpenApi3(content);
            default -> throw new IllegalArgumentException("Format not supported by SwaggerParser: " + format);
        };
    }


    private ParsedSpec parseSwagger2(String content) throws Exception {
        log.debug("Parsing Swagger 2.0");
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);

        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);

        if (result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Unable to parse Swagger 2.0: " +
                String.join(", ", result.getMessages()));
        }

        return new ParsedSpec(SpecFormat.SWAGGER_2, result.getOpenAPI());
    }

    private ParsedSpec parseOpenApi3(String content) throws Exception {
        log.debug("Parsing OpenAPI 3.x");
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);

        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);

        if (result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Unable to parse OpenAPI 3.x: " +
                String.join(", ", result.getMessages()));
        }

        return new ParsedSpec(SpecFormat.OPENAPI_3, result.getOpenAPI());
    }

    private JsonNode parseJson(String content) throws Exception {
        try {
            return jsonMapper.readTree(content);
        } catch (Exception e) {
            return yamlMapper.readTree(content);
        }
    }
}
