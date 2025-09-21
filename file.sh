#!/bin/bash

# Script per refactorizzare ApiSpecConverter in classi specializzate
# Eseguire dalla directory root del progetto Maven

echo "🔄 Refactoring ApiSpecConverter in classi specializzate..."

BASE_PACKAGE_DIR="src/main/java/io/github/apitools/maven"

# Crea le directory per i package specializzati
mkdir -p $BASE_PACKAGE_DIR/parsers
mkdir -p $BASE_PACKAGE_DIR/converters
mkdir -p $BASE_PACKAGE_DIR/serializers
mkdir -p $BASE_PACKAGE_DIR/utils

echo "📁 Directory create"

# === PARSERS ===

# SwaggerParser.java
cat > $BASE_PACKAGE_DIR/parsers/SwaggerParser.java << 'EOF'
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
        switch (format) {
            case SWAGGER_1:
                return parseSwagger1(content);
            case SWAGGER_2:
                return parseSwagger2(content);
            case OPENAPI_3:
                return parseOpenApi3(content);
            default:
                throw new IllegalArgumentException("Formato non supportato dal SwaggerParser: " + format);
        }
    }

    private ParsedSpec parseSwagger1(String content) throws Exception {
        log.debug("Parsing Swagger 1.x");
        JsonNode jsonNode = parseJson(content);
        return new ParsedSpec(SpecFormat.SWAGGER_1, jsonNode);
    }

    private ParsedSpec parseSwagger2(String content) throws Exception {
        log.debug("Parsing Swagger 2.0");
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);

        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);

        if (result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Impossibile parsare Swagger 2.0: " +
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
            throw new IllegalArgumentException("Impossibile parsare OpenAPI 3.x: " +
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
EOF

# RamlParser.java
cat > $BASE_PACKAGE_DIR/parsers/RamlParser.java << 'EOF'
package io.github.apitools.maven.parsers;

import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.api.Api;

public class RamlParser {

    private final Log log;

    public RamlParser(Log log) {
        this.log = log;
    }

    public ParsedSpec parse(String content) throws Exception {
        log.debug("Parsing RAML");

        RamlModelResult ramlResult = new RamlModelBuilder().buildApi(content);

        if (ramlResult.hasErrors()) {
            throw new IllegalArgumentException("Errori nel parsing RAML: " +
                ramlResult.getValidationResults().toString());
        }

        Api api = ramlResult.getApiV10();
        return new ParsedSpec(SpecFormat.RAML, api);
    }
}
EOF

# WadlParser.java
cat > $BASE_PACKAGE_DIR/parsers/WadlParser.java << 'EOF'
package io.github.apitools.maven.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

public class WadlParser {

    private final Log log;
    private final ObjectMapper jsonMapper;

    public WadlParser(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
    }

    public ParsedSpec parse(String content) throws Exception {
        log.debug("Parsing WADL");
        JsonNode jsonNode = parseXmlToJson(content);
        return new ParsedSpec(SpecFormat.WADL, jsonNode);
    }

    private JsonNode parseXmlToJson(String xmlContent) throws Exception {
        throw new UnsupportedOperationException("WADL parsing non ancora implementato completamente");
    }
}
EOF

# ApiBlueprintParser.java
cat > $BASE_PACKAGE_DIR/parsers/ApiBlueprintParser.java << 'EOF'
package io.github.apitools.maven.parsers;

import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

public class ApiBlueprintParser {

    private final Log log;

    public ApiBlueprintParser(Log log) {
        this.log = log;
    }

    public ParsedSpec parse(String content) throws Exception {
        log.debug("Parsing API Blueprint");
        return new ParsedSpec(SpecFormat.API_BLUEPRINT, content);
    }
}
EOF

# IoDocsParser.java
cat > $BASE_PACKAGE_DIR/parsers/IoDocsParser.java << 'EOF'
package io.github.apitools.maven.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

public class IoDocsParser {

    private final Log log;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public IoDocsParser(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();
    }

    public ParsedSpec parse(String content) throws Exception {
        log.debug("Parsing I/O Docs");
        JsonNode jsonNode = parseJson(content);
        return new ParsedSpec(SpecFormat.IO_DOCS, jsonNode);
    }

    private JsonNode parseJson(String content) throws Exception {
        try {
            return jsonMapper.readTree(content);
        } catch (Exception e) {
            return yamlMapper.readTree(content);
        }
    }
}
EOF

# GoogleDiscoveryParser.java
cat > $BASE_PACKAGE_DIR/parsers/GoogleDiscoveryParser.java << 'EOF'
package io.github.apitools.maven.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

public class GoogleDiscoveryParser {

    private final Log log;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public GoogleDiscoveryParser(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();
    }

    public ParsedSpec parse(String content) throws Exception {
        log.debug("Parsing Google Discovery");
        JsonNode jsonNode = parseJson(content);
        return new ParsedSpec(SpecFormat.GOOGLE, jsonNode);
    }

    private JsonNode parseJson(String content) throws Exception {
        try {
            return jsonMapper.readTree(content);
        } catch (Exception e) {
            return yamlMapper.readTree(content);
        }
    }
}
EOF

echo "✅ Parser creati"

# === CONVERTERS ===

# ToOpenApiConverter.java
cat > $BASE_PACKAGE_DIR/converters/ToOpenApiConverter.java << 'EOF'
package io.github.apitools.maven.converters;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToOpenApiConverter {

    private final Log log;

    public ToOpenApiConverter(Log log) {
        this.log = log;
    }

    public OpenAPI convertToOpenAPI(ParsedSpec parsedSpec) throws Exception {
        SpecFormat sourceFormat = parsedSpec.getFormat();

        if (sourceFormat == SpecFormat.OPENAPI_3 && parsedSpec.getData() instanceof OpenAPI) {
            return (OpenAPI) parsedSpec.getData();
        }

        if (sourceFormat == SpecFormat.SWAGGER_2 && parsedSpec.getData() instanceof OpenAPI) {
            return (OpenAPI) parsedSpec.getData();
        }

        switch (sourceFormat) {
            case SWAGGER_1:
                return convertSwagger1ToOpenAPI((JsonNode) parsedSpec.getData());
            case RAML:
                return convertRamlToOpenAPI((Api) parsedSpec.getData());
            case WADL:
                return convertWadlToOpenAPI((JsonNode) parsedSpec.getData());
            case API_BLUEPRINT:
                return convertApiBlueprintToOpenAPI((String) parsedSpec.getData());
            case IO_DOCS:
                return convertIoDocsToOpenAPI((JsonNode) parsedSpec.getData());
            case GOOGLE:
                return convertGoogleToOpenAPI((JsonNode) parsedSpec.getData());
            default:
                throw new IllegalArgumentException("Conversione non supportata da: " + sourceFormat);
        }
    }

    private OpenAPI convertSwagger1ToOpenAPI(JsonNode swagger1) {
        log.debug("Conversione Swagger 1.x -> OpenAPI");

        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");

        Info info = new Info();
        if (swagger1.has("info")) {
            JsonNode infoNode = swagger1.get("info");
            if (infoNode.has("title")) info.title(infoNode.get("title").asText());
            if (infoNode.has("version")) info.version(infoNode.get("version").asText());
            if (infoNode.has("description")) info.description(infoNode.get("description").asText());
        } else {
            info.title("Converted API").version("1.0.0");
        }
        openAPI.info(info);

        io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
        if (swagger1.has("apis")) {
            swagger1.get("apis").forEach(api -> {
                if (api.has("path")) {
                    String path = api.get("path").asText();
                    PathItem pathItem = new PathItem();
                    Operation operation = new Operation();
                    operation.summary("Converted from Swagger 1.x");
                    operation.responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Success")));
                    pathItem.get(operation);
                    paths.addPathItem(path, pathItem);
                }
            });
        }
        openAPI.paths(paths);
        return openAPI;
    }

    private OpenAPI convertRamlToOpenAPI(Api ramlApi) {
        log.debug("Conversione RAML -> OpenAPI");

        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");

        Info info = new Info();
        info.title(ramlApi.title() != null ? ramlApi.title().value() : "Converted from RAML");
        info.version(ramlApi.version() != null ? ramlApi.version().value() : "1.0.0");
        if (ramlApi.description() != null) info.description(ramlApi.description().value());
        openAPI.info(info);

        if (ramlApi.baseUri() != null) {
            Server server = new Server();
            server.url(ramlApi.baseUri().value());
            openAPI.addServersItem(server);
        }

        io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
        convertRamlResources(ramlApi.resources(), paths, "");
        openAPI.paths(paths);

        return openAPI;
    }

    private void convertRamlResources(List<Resource> resources, io.swagger.v3.oas.models.Paths paths, String parentPath) {
        for (Resource resource : resources) {
            String fullPath = parentPath + resource.relativeUri().value();
            PathItem pathItem = new PathItem();

            for (Method method : resource.methods()) {
                Operation operation = new Operation();

                if (method.displayName() != null) operation.summary(method.displayName().value());
                if (method.description() != null) operation.description(method.description().value());

                method.queryParameters().forEach(param -> {
                    Parameter openApiParam = new QueryParameter();
                    openApiParam.setName(param.name());
                    if (param.description() != null) {
                        openApiParam.setDescription(param.description().value());
                    }
                    operation.addParametersItem(openApiParam);
                });

                ApiResponses responses = new ApiResponses();
                if (method.responses().isEmpty()) {
                    responses.addApiResponse("200", new ApiResponse().description("Success"));
                } else {
                    method.responses().forEach(response -> {
                        String code = response.code().value();
                        ApiResponse apiResponse = new ApiResponse();
                        apiResponse.description(response.description() != null ?
                            response.description().value() : "Response " + code);
                        responses.addApiResponse(code, apiResponse);
                    });
                }
                operation.responses(responses);

                switch (method.method().toLowerCase()) {
                    case "get": pathItem.get(operation); break;
                    case "post": pathItem.post(operation); break;
                    case "put": pathItem.put(operation); break;
                    case "delete": pathItem.delete(operation); break;
                    case "patch": pathItem.patch(operation); break;
                    case "head": pathItem.head(operation); break;
                    case "options": pathItem.options(operation); break;
                }
            }

            paths.addPathItem(fullPath, pathItem);

            if (!resource.resources().isEmpty()) {
                convertRamlResources(resource.resources(), paths, fullPath);
            }
        }
    }

    private OpenAPI convertWadlToOpenAPI(JsonNode wadl) {
        log.debug("Conversione WADL -> OpenAPI");
        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");
        openAPI.info(new Info().title("Converted from WADL").version("1.0.0"));
        openAPI.paths(new io.swagger.v3.oas.models.Paths());
        return openAPI;
    }

    private OpenAPI convertApiBlueprintToOpenAPI(String blueprint) {
        log.debug("Conversione API Blueprint -> OpenAPI");

        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");

        Info info = new Info();
        Pattern titlePattern = Pattern.compile("(?m)^#\\s+(.+)$");
        Matcher titleMatcher = titlePattern.matcher(blueprint);
        if (titleMatcher.find()) {
            info.title(titleMatcher.group(1).trim());
        } else {
            info.title("Converted from API Blueprint");
        }
        info.version("1.0.0");
        openAPI.info(info);

        io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
        Pattern pathPattern = Pattern.compile("(?m)^##\\s+(.+)\\s+\\[(.+)\\]$");
        Matcher pathMatcher = pathPattern.matcher(blueprint);

        while (pathMatcher.find()) {
            String pathName = pathMatcher.group(2);
            String description = pathMatcher.group(1);

            PathItem pathItem = new PathItem();
            Operation operation = new Operation()
                .summary(description)
                .responses(new ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("Success")));

            pathItem.get(operation);
            paths.addPathItem(pathName, pathItem);
        }

        openAPI.paths(paths);
        return openAPI;
    }

    private OpenAPI convertIoDocsToOpenAPI(JsonNode ioDocs) {
        log.debug("Conversione I/O Docs -> OpenAPI");

        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");

        Info info = new Info();
        info.title(ioDocs.has("name") ? ioDocs.get("name").asText() : "Converted from I/O Docs");
        info.version(ioDocs.has("version") ? ioDocs.get("version").asText() : "1.0.0");
        openAPI.info(info);

        if (ioDocs.has("baseURL")) {
            Server server = new Server();
            server.url(ioDocs.get("baseURL").asText());
            openAPI.addServersItem(server);
        }

        io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
        if (ioDocs.has("methods")) {
            ioDocs.get("methods").forEach(method -> {
                if (method.has("URI")) {
                    String uri = method.get("URI").asText();
                    PathItem pathItem = new PathItem();

                    Operation operation = new Operation();
                    if (method.has("Synopsis")) {
                        operation.summary(method.get("Synopsis").asText());
                    }
                    operation.responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Success")));

                    String httpMethod = method.has("HTTPMethod") ?
                        method.get("HTTPMethod").asText().toLowerCase() : "get";

                    switch (httpMethod) {
                        case "get": pathItem.get(operation); break;
                        case "post": pathItem.post(operation); break;
                        case "put": pathItem.put(operation); break;
                        case "delete": pathItem.delete(operation); break;
                    }

                    paths.addPathItem(uri, pathItem);
                }
            });
        }

        openAPI.paths(paths);
        return openAPI;
    }

    private OpenAPI convertGoogleToOpenAPI(JsonNode google) {
        log.debug("Conversione Google Discovery -> OpenAPI");

        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");

        Info info = new Info();
        info.title(google.has("name") ? google.get("name").asText() : "Converted from Google Discovery");
        info.version(google.has("version") ? google.get("version").asText() : "1.0.0");
        if (google.has("description")) info.description(google.get("description").asText());
        openAPI.info(info);

        if (google.has("baseUrl")) {
            Server server = new Server();
            server.url(google.get("baseUrl").asText());
            openAPI.addServersItem(server);
        }

        io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
        if (google.has("resources")) {
            convertGoogleResources(google.get("resources"), paths, "");
        }

        openAPI.paths(paths);
        return openAPI;
    }

    private void convertGoogleResources(JsonNode resources, io.swagger.v3.oas.models.Paths paths, String parentPath) {
        resources.fields().forEachRemaining(entry -> {
            JsonNode resource = entry.getValue();
            if (resource.has("methods")) {
                resource.get("methods").fields().forEachRemaining(methodEntry -> {
                    JsonNode method = methodEntry.getValue();
                    if (method.has("path")) {
                        String path = parentPath + method.get("path").asText();
                        PathItem pathItem = new PathItem();

                        Operation operation = new Operation();
                        if (method.has("description")) {
                            operation.summary(method.get("description").asText());
                        }
                        operation.responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse().description("Success")));

                        String httpMethod = method.has("httpMethod") ?
                            method.get("httpMethod").asText().toLowerCase() : "get";

                        switch (httpMethod) {
                            case "get": pathItem.get(operation); break;
                            case "post": pathItem.post(operation); break;
                            case "put": pathItem.put(operation); break;
                            case "delete": pathItem.delete(operation); break;
                        }

                        paths.addPathItem(path, pathItem);
                    }
                });
            }

            if (resource.has("resources")) {
                convertGoogleResources(resource.get("resources"), paths, parentPath);
            }
        });
    }
}
EOF

# FromOpenApiConverter.java
cat > $BASE_PACKAGE_DIR/converters/FromOpenApiConverter.java << 'EOF'
package io.github.apitools.maven.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.github.apitools.maven.ConversionOptions;
import io.github.apitools.maven.OutputSyntax;
import io.github.apitools.maven.SpecFormat;
import org.apache.maven.plugin.logging.Log;

import java.net.URI;

public class FromOpenApiConverter {

    private final Log log;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public FromOpenApiConverter(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();
    }

    public String convertFromOpenAPI(OpenAPI openAPI, ConversionOptions options) throws Exception {
        switch (options.getTo()) {
            case SWAGGER_2:
                return convertOpenAPIToSwagger2(openAPI, options);
            case OPENAPI_3:
                return serializeOpenAPI(openAPI, options);
            default:
                throw new IllegalArgumentException("Conversione non supportata verso: " + options.getTo());
        }
    }

    private String serializeOpenAPI(OpenAPI openAPI, ConversionOptions options) {
        if (options.getSyntax() == OutputSyntax.YAML) {
            return Yaml.pretty(openAPI);
        } else {
            return Json.pretty(openAPI);
        }
    }

    private String convertOpenAPIToSwagger2(OpenAPI openAPI, ConversionOptions options) throws Exception {
        log.debug("Conversione OpenAPI -> Swagger 2.0");

        ObjectNode swagger2 = jsonMapper.createObjectNode();
        swagger2.put("swagger", "2.0");

        if (openAPI.getInfo() != null) {
            ObjectNode info = jsonMapper.valueToTree(openAPI.getInfo());
            swagger2.set("info", info);
        }

        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            String serverUrl = openAPI.getServers().get(0).getUrl();
            try {
                URI uri = URI.create(serverUrl);
                swagger2.put("host", uri.getHost());
                swagger2.put("basePath", uri.getPath());
                swagger2.set("schemes", jsonMapper.createArrayNode().add(uri.getScheme()));
            } catch (Exception e) {
                log.warn("Could not parse server URL: " + serverUrl);
            }
        }

        if (openAPI.getPaths() != null) {
            ObjectNode paths = convertPathsToSwagger2(openAPI.getPaths());
            swagger2.set("paths", paths);
        }

        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            ObjectNode definitions = jsonMapper.valueToTree(openAPI.getComponents().getSchemas());
            swagger2.set("definitions", definitions);
        }

        if (options.getSyntax() == OutputSyntax.YAML) {
            return yamlMapper.writeValueAsString(swagger2);
        } else {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger2);
        }
    }

    private ObjectNode convertPathsToSwagger2(Paths paths) {
        ObjectNode swagger2Paths = jsonMapper.createObjectNode();

        paths.forEach((pathName, pathItem) -> {
            ObjectNode swagger2PathItem = jsonMapper.createObjectNode();

            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                ObjectNode swagger2Operation = jsonMapper.createObjectNode();

                if (operation.getSummary() != null) {
                    swagger2Operation.put("summary", operation.getSummary());
                }
                if (operation.getDescription() != null) {
                    swagger2Operation.put("description", operation.getDescription());
                }
                if (operation.getOperationId() != null) {
                    swagger2Operation.put("operationId", operation.getOperationId());
                }

                if (operation.getParameters() != null) {
                    swagger2Operation.set("parameters",
                        jsonMapper.valueToTree(operation.getParameters()));
                }

                if (operation.getResponses() != null) {
                    ObjectNode responses = jsonMapper.createObjectNode();
                    operation.getResponses().forEach((code, response) -> {
                        ObjectNode swagger2Response = jsonMapper.createObjectNode();
                        if (response.getDescription() != null) {
                            swagger2Response.put("description", response.getDescription());
                        }
                        responses.set(code, swagger2Response);
                    });
                    swagger2Operation.set("responses", responses);
                }

                swagger2PathItem.set(httpMethod.name().toLowerCase(), swagger2Operation);
            });

            swagger2Paths.set(pathName, swagger2PathItem);
        });

        return swagger2Paths;
    }
}
EOF

echo "✅ Converter creati"

# === UTILS ===

# OpenApiFieldFiller.java
cat > $BASE_PACKAGE_DIR/utils/OpenApiFieldFiller.java << 'EOF'
package io.github.apitools.maven.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public class OpenApiFieldFiller {

    public static void fillMissingFields(OpenAPI openAPI) {
        if (openAPI.getInfo() == null) {
            openAPI.info(new Info()
                .title("Generated API")
                .version("1.0.0"));
        }

        if (openAPI.getInfo().getTitle() == null) {
            openAPI.getInfo().setTitle("Generated API");
        }

        if (openAPI.getInfo().getVersion() == null) {
            openAPI.getInfo().setVersion("1.0.0");
        }

        if (openAPI.getPaths() == null) {
            openAPI.paths(new io.swagger.v3.oas.models.Paths());
        }
    }
}
EOF

# OpenApiValidator.java
cat > $BASE_PACKAGE_DIR/utils/OpenApiValidator.java << 'EOF'
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
            warnings.add("Errore durante la validazione: " + e.getMessage());
        }

        return warnings;
    }
}
EOF

echo "✅ Utils creati"

# === NUOVO ApiSpecConverter REFACTORIZZATO ===

cat > $BASE_PACKAGE_DIR/ApiSpecConverter.java << 'EOF'
package io.github.apitools.maven;

import io.swagger.v3.oas.models.OpenAPI;
import io.github.apitools.maven.converters.*;
import io.github.apitools.maven.parsers.*;
import io.github.apitools.maven.utils.*;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Convertitore principale che coordina le varie implementazioni specializzate.
 */
public class ApiSpecConverter {

    private final Log log;
    private final HttpClient httpClient;

    // Parsers
    private final SwaggerParser swaggerParser;
    private final RamlParser ramlParser;
    private final WadlParser wadlParser;
    private final ApiBlueprintParser apiBlueprintParser;
    private final IoDocsParser ioDocsParser;
    private final GoogleDiscoveryParser googleDiscoveryParser;

    // Converters
    private final ToOpenApiConverter toOpenApiConverter;
    private final FromOpenApiConverter fromOpenApiConverter;

    public ApiSpecConverter(Log log) {
        this.log = log;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

        // Initialize parsers
        this.swaggerParser = new SwaggerParser(log);
        this.ramlParser = new RamlParser(log);
        this.wadlParser = new WadlParser(log);
        this.apiBlueprintParser = new ApiBlueprintParser(log);
        this.ioDocsParser = new IoDocsParser(log);
        this.googleDiscoveryParser = new GoogleDiscoveryParser(log);

        // Initialize converters
        this.toOpenApiConverter = new ToOpenApiConverter(log);
        this.fromOpenApiConverter = new FromOpenApiConverter(log);
    }

    /**
     * Converte una specifica API da un formato all'altro.
     */
    public ConversionResult convert(ConversionOptions options) throws Exception {
        log.info("Inizio conversione da " + options.getFrom() + " a " + options.getTo());

        // Read source content
        String sourceContent = readSource(options.getSource());
        log.debug("Contenuto sorgente letto: " + sourceContent.length() + " caratteri");

        // Parse source format
        ParsedSpec parsedSpec = parseSource(sourceContent, options.getFrom());

        // Convert to target format
        String convertedContent = convertToTarget(parsedSpec, options);

        // Validate if requested
        List<String> warnings = new ArrayList<>();
        if (options.isValidate()) {
            warnings.addAll(OpenApiValidator.validate(convertedContent, options.getTo()));
        }

        return new ConversionResult(convertedContent, warnings);
    }

    private String readSource(String source) throws Exception {
        if (isUrl(source)) {
            return readFromUrl(source);
        } else {
            return readFromFile(source);
        }
    }

    private boolean isUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private String readFromUrl(String url) throws Exception {
        log.info("Lettura da URL: " + url);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Errore HTTP " + response.statusCode() +
                " durante la lettura da " + url);
        }

        return response.body();
    }

    private String readFromFile(String filePath) throws Exception {
        log.info("Lettura da file: " + filePath);

        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File non trovato: " + filePath);
        }

        return Files.readString(path);
    }

    private ParsedSpec parseSource(String content, SpecFormat format) throws Exception {
        log.info("Parsing del formato: " + format);

        switch (format) {
            case SWAGGER_1:
            case SWAGGER_2:
            case OPENAPI_3:
                return swaggerParser.parse(content, format);
            case RAML:
                return ramlParser.parse(content);
            case WADL:
                return wadlParser.parse(content);
            case API_BLUEPRINT:
                return apiBlueprintParser.parse(content);
            case IO_DOCS:
                return ioDocsParser.parse(content);
            case GOOGLE:
                return googleDiscoveryParser.parse(content);
            default:
                throw new IllegalArgumentException("Formato non supportato: " + format);
        }
    }

    private String convertToTarget(ParsedSpec parsedSpec, ConversionOptions options) throws Exception {
        log.info("Conversione verso il formato: " + options.getTo());

        // Convert everything to OpenAPI first
        OpenAPI openAPI = toOpenApiConverter.convertToOpenAPI(parsedSpec);

        // Apply missing fields if requested
        if (options.isFillMissing()) {
            OpenApiFieldFiller.fillMissingFields(openAPI);
        }

        // Then convert from OpenAPI to target format
        return fromOpenApiConverter.convertFromOpenAPI(openAPI, options);
    }
}
EOF

echo "✅ ApiSpecConverter refactorizzato creato"

# === TEST ===

# Test di esempio per verificare la nuova struttura
cat > src/test/java/$BASE_PACKAGE_DIR/RefactoredConverterTest.java << 'EOF'
package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.apache.maven.plugin.logging.Log;

public class RefactoredConverterTest {

    private Log mockLog;
    private ApiSpecConverter converter;

    @BeforeEach
    public void setUp() {
        mockLog = mock(Log.class);
        converter = new ApiSpecConverter(mockLog);
    }

    @Test
    public void testConverterInitialization() {
        assertNotNull(converter);
    }

    @Test
    public void testBasicConversionOptions() {
        ConversionOptions options = ConversionOptions.builder()
            .source("test.json")
            .from(SpecFormat.SWAGGER_2)
            .to(SpecFormat.OPENAPI_3)
            .syntax(OutputSyntax.JSON)
            .build();

        assertNotNull(options);
        assertEquals("test.json", options.getSource());
        assertEquals(SpecFormat.SWAGGER_2, options.getFrom());
        assertEquals(SpecFormat.OPENAPI_3, options.getTo());
        assertEquals(OutputSyntax.JSON, options.getSyntax());
    }
}
EOF

echo "✅ Test di esempio creato"

echo ""
echo "🎉 Refactoring completato!"
echo ""
echo "Struttura creata:"
echo "├── parsers/"
echo "│   ├── SwaggerParser.java"
echo "│   ├── RamlParser.java"
echo "│   ├── WadlParser.java"
echo "│   ├── ApiBlueprintParser.java"
echo "│   ├── IoDocsParser.java"
echo "│   └── GoogleDiscoveryParser.java"
echo "├── converters/"
echo "│   ├── ToOpenApiConverter.java"
echo "│   └── FromOpenApiConverter.java"
echo "├── utils/"
echo "│   ├── OpenApiFieldFiller.java"
echo "│   └── OpenApiValidator.java"
echo "└── ApiSpecConverter.java (refactorizzato)"
echo ""
echo "Prossimi passi:"
echo "1. mvn clean compile"
echo "2. mvn clean install"
echo "3. Testare il plugin refactorizzato"
echo ""
echo "Per testare:"
echo "mvn io.github.apitools:api-spec-converter-maven-plugin:1.0.0:convert \\"
echo "  -Dsource=https://petstore.swagger.io/v2/swagger.json \\"
echo "  -Dfrom=swagger_2 \\"
echo "  -Dto=openapi_3 \\"
echo "  -Dsyntax=yaml"
echo ""

# Rendi lo script eseguibile
chmod +x refactor-api-converter.sh

echo "Script creato! Esegui './refactor-api-converter.sh' dalla directory del progetto Maven."