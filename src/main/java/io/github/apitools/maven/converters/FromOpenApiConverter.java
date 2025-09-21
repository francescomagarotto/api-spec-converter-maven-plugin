package io.github.apitools.maven.converters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.apitools.maven.ConversionOptions;
import io.github.apitools.maven.OutputSyntax;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.maven.plugin.logging.Log;

import java.util.Map;

public class FromOpenApiConverter {

    private final Log log;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;
    private OpenAPI currentOpenAPI;

    public FromOpenApiConverter(Log log) {
        this.log = log;
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();

        this.jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String convertFromOpenAPI(OpenAPI openAPI, ConversionOptions options) throws Exception {
        this.currentOpenAPI = openAPI;

        switch (options.getTo()) {
            case SWAGGER_2:
                return convertOpenAPIToSwagger2(openAPI, options);
            case OPENAPI_3:
                return serializeOpenAPI(openAPI, options);
            default:
                throw new IllegalArgumentException("Conversion not supported to: " + options.getTo());
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
        log.debug("Converting OpenAPI -> Swagger 2.0 (validation fix)");

        ObjectNode swagger2 = jsonMapper.createObjectNode();
        swagger2.put("swagger", "2.0");

        // Required info
        ObjectNode info = jsonMapper.createObjectNode();
        if (openAPI.getInfo() != null) {
            info.put("title", openAPI.getInfo().getTitle() != null ? openAPI.getInfo().getTitle() : "API");
            info.put("version", openAPI.getInfo().getVersion() != null ? openAPI.getInfo().getVersion() : "1.0.0");
            if (openAPI.getInfo().getDescription() != null) {
                info.put("description", openAPI.getInfo().getDescription());
            }
        } else {
            info.put("title", "API");
            info.put("version", "1.0.0");
        }
        swagger2.set("info", info);

        // Paths
        if (openAPI.getPaths() != null && !openAPI.getPaths().isEmpty()) {
            ObjectNode paths = convertPaths(openAPI.getPaths());
            swagger2.set("paths", paths);
        }

        // Definitions
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            ObjectNode definitions = convertDefinitions(openAPI.getComponents().getSchemas());
            swagger2.set("definitions", definitions);
        }

        // Global parameters
        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            ObjectNode parameters = convertGlobalParameters(openAPI.getComponents().getParameters());
            swagger2.set("parameters", parameters);
        }

        // Global responses
        if (openAPI.getComponents() != null && openAPI.getComponents().getResponses() != null) {
            ObjectNode responses = convertGlobalResponses(openAPI.getComponents().getResponses());
            swagger2.set("responses", responses);
        }

        if (options.getSyntax() == OutputSyntax.YAML) {
            return yamlMapper.writeValueAsString(swagger2);
        } else {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger2);
        }
    }

    private ObjectNode convertPaths(io.swagger.v3.oas.models.Paths paths) {
        ObjectNode swagger2Paths = jsonMapper.createObjectNode();

        paths.forEach((pathName, pathItem) -> {
            ObjectNode swagger2PathItem = jsonMapper.createObjectNode();

            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                ObjectNode swagger2Operation = convertOperation(operation);
                swagger2PathItem.set(httpMethod.name().toLowerCase(), swagger2Operation);
            });

            swagger2Paths.set(pathName, swagger2PathItem);
        });

        return swagger2Paths;
    }

    private ObjectNode convertOperation(Operation operation) {
        ObjectNode swagger2Op = jsonMapper.createObjectNode();

        if (operation.getSummary() != null) swagger2Op.put("summary", operation.getSummary());
        if (operation.getDescription() != null) swagger2Op.put("description", operation.getDescription());
        if (operation.getOperationId() != null) swagger2Op.put("operationId", operation.getOperationId());

        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            ArrayNode tags = jsonMapper.createArrayNode();
            operation.getTags().forEach(tags::add);
            swagger2Op.set("tags", tags);
        }

        // Parameters with FIX for required 'type'
        ArrayNode parameters = jsonMapper.createArrayNode();

        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                ObjectNode swagger2Param = convertParameterWithTypeRequired(param);
                parameters.add(swagger2Param);
            }
        }

        // RequestBody -> body parameter
        if (operation.getRequestBody() != null) {
            ObjectNode bodyParam = convertRequestBodyToBodyParam(operation.getRequestBody());
            if (bodyParam != null) parameters.add(bodyParam);
        }

        if (parameters.size() > 0) swagger2Op.set("parameters", parameters);

        // Responses with required description
        ObjectNode responses = convertResponsesWithDescriptionRequired(operation.getResponses());
        swagger2Op.set("responses", responses);

        return swagger2Op;
    }

    private ObjectNode convertParameterWithTypeRequired(Parameter param) {
        ObjectNode swagger2Param = jsonMapper.createObjectNode();

        // If the parameter has a $ref, return only the $ref (according to Swagger 2 specs)
        if (param.get$ref() != null) {
            String ref = param.get$ref();
            // Convert from #/components/parameters/ to #/parameters/
            if (ref.startsWith("#/components/parameters/")) {
                ref = ref.replace("#/components/parameters/", "#/parameters/");
            }
            swagger2Param.put("$ref", ref);
            return swagger2Param;
        }

        // Check if this parameter matches a global parameter
        // If yes, use a $ref instead of expanding properties
        String matchingGlobalParam = findMatchingGlobalParameter(param);
        if (matchingGlobalParam != null) {
            swagger2Param.put("$ref", "#/parameters/" + matchingGlobalParam);
            return swagger2Param;
        }

        // Required properties
        swagger2Param.put("name", param.getName() != null ? param.getName() : "param");
        swagger2Param.put("in", param.getIn() != null ? param.getIn() : "query");

        if (param.getDescription() != null) swagger2Param.put("description", param.getDescription());
        if (param.getRequired() != null) swagger2Param.put("required", param.getRequired());

        // FIX: type is REQUIRED in Swagger 2.0 (except for $ref)
        if (param.getSchema() != null) {
            if (param.getSchema().get$ref() != null) {
                // For parameters, the schema $ref must be resolved and converted to type
                // Only parameters with direct $ref (not schema $ref) use $ref in Swagger 2
                Schema<?> resolvedSchema = resolveSchemaReference(param.getSchema().get$ref());
                if (resolvedSchema != null) {
                    String type = resolvedSchema.getType();
                    if (type == null) type = "string";
                    swagger2Param.put("type", type);

                    if (resolvedSchema.getFormat() != null) {
                        swagger2Param.put("format", resolvedSchema.getFormat());
                    }
                    if (resolvedSchema.getDefault() != null) {
                        swagger2Param.set("default", jsonMapper.valueToTree(resolvedSchema.getDefault()));
                    }
                    if (resolvedSchema.getMinimum() != null) {
                        swagger2Param.put("minimum", resolvedSchema.getMinimum().doubleValue());
                    }
                    if (resolvedSchema.getMaximum() != null) {
                        swagger2Param.put("maximum", resolvedSchema.getMaximum().doubleValue());
                    }
                } else {
                    swagger2Param.put("type", "string");
                }
            } else {
                // Otherwise, type is required
                String type = param.getSchema().getType();
                if (type == null) type = "string"; // default fallback
                swagger2Param.put("type", type);

                if (param.getSchema().getFormat() != null) {
                    swagger2Param.put("format", param.getSchema().getFormat());
                }
                if (param.getSchema().getDefault() != null) {
                    swagger2Param.set("default", jsonMapper.valueToTree(param.getSchema().getDefault()));
                }
                if (param.getSchema().getMinimum() != null) {
                    swagger2Param.put("minimum", param.getSchema().getMinimum().doubleValue());
                }
                if (param.getSchema().getMaximum() != null) {
                    swagger2Param.put("maximum", param.getSchema().getMaximum().doubleValue());
                }
            }
        } else {
            // Missing schema, use string type as fallback
            swagger2Param.put("type", "string");
        }

        return swagger2Param;
    }

    private ObjectNode convertRequestBodyToBodyParam(RequestBody requestBody) {
        if (requestBody.getContent() == null || requestBody.getContent().isEmpty()) return null;

        ObjectNode bodyParam = jsonMapper.createObjectNode();
        bodyParam.put("name", "body");
        bodyParam.put("in", "body");

        if (requestBody.getDescription() != null) {
            bodyParam.put("description", requestBody.getDescription());
        }
        if (requestBody.getRequired() != null && requestBody.getRequired()) {
            bodyParam.put("required", true);
        }

        requestBody.getContent().values().stream().findFirst().ifPresent(mediaType -> {
            if (mediaType.getSchema() != null) {
                ObjectNode schema = convertSchemaToSwagger2(mediaType.getSchema());
                bodyParam.set("schema", schema);
            }
        });

        return bodyParam;
    }

    private ObjectNode convertResponsesWithDescriptionRequired(ApiResponses responses) {
        ObjectNode swagger2Responses = jsonMapper.createObjectNode();

        if (responses != null) {
            responses.forEach((code, response) -> {
                // Resolve $ref
                if (response.get$ref() != null) {
                    ApiResponse resolved = resolveResponseReference(response.get$ref());
                    if (resolved != null) response = resolved;
                }

                ObjectNode swagger2Response = jsonMapper.createObjectNode();

                // FIX: description è OBBLIGATORIO in Swagger 2.0
                String description = response.getDescription();
                if (description == null || description.trim().isEmpty()) {
                    description = "Response " + code;
                }
                swagger2Response.put("description", description);

                if (response.getContent() != null && !response.getContent().isEmpty()) {
                    response.getContent().values().stream().findFirst().ifPresent(mediaType -> {
                        if (mediaType.getSchema() != null) {
                            ObjectNode schema = convertSchemaToSwagger2(mediaType.getSchema());
                            swagger2Response.set("schema", schema);
                        }
                    });
                }

                swagger2Responses.set(code, swagger2Response);
            });
        }

        return swagger2Responses;
    }

    private ObjectNode convertDefinitions(Map<String, Schema> schemas) {
        ObjectNode definitions = jsonMapper.createObjectNode();

        schemas.forEach((name, schema) -> {
            ObjectNode swagger2Schema = convertSchemaToSwagger2(schema);
            definitions.set(name, swagger2Schema);
        });

        return definitions;
    }

    private ObjectNode convertGlobalParameters(Map<String, Parameter> parameters) {
        ObjectNode swagger2Params = jsonMapper.createObjectNode();

        parameters.forEach((name, param) -> {
            ObjectNode swagger2Param = convertGlobalParameterDefinition(param);
            swagger2Params.set(name, swagger2Param);
        });

        return swagger2Params;
    }

    private ObjectNode convertGlobalParameterDefinition(Parameter param) {
        ObjectNode swagger2Param = jsonMapper.createObjectNode();

        // For global definitions, never use $ref - always expand properties
        swagger2Param.put("name", param.getName() != null ? param.getName() : "param");
        swagger2Param.put("in", param.getIn() != null ? param.getIn() : "query");

        if (param.getDescription() != null) swagger2Param.put("description", param.getDescription());
        if (param.getRequired() != null) swagger2Param.put("required", param.getRequired());

        // FIX: type is REQUIRED in Swagger 2.0 (except for $ref)
        if (param.getSchema() != null) {
            if (param.getSchema().get$ref() != null) {
                // Per i parametri, il $ref dello schema deve essere risolto e convertito in type
                Schema<?> resolvedSchema = resolveSchemaReference(param.getSchema().get$ref());
                if (resolvedSchema != null) {
                    String type = resolvedSchema.getType();
                    if (type == null) type = "string";
                    swagger2Param.put("type", type);

                    if (resolvedSchema.getFormat() != null) {
                        swagger2Param.put("format", resolvedSchema.getFormat());
                    }
                    if (resolvedSchema.getDefault() != null) {
                        swagger2Param.set("default", jsonMapper.valueToTree(resolvedSchema.getDefault()));
                    }
                    if (resolvedSchema.getMinimum() != null) {
                        swagger2Param.put("minimum", resolvedSchema.getMinimum().doubleValue());
                    }
                    if (resolvedSchema.getMaximum() != null) {
                        swagger2Param.put("maximum", resolvedSchema.getMaximum().doubleValue());
                    }
                } else {
                    swagger2Param.put("type", "string");
                }
            } else {
                // Otherwise, type is required
                String type = param.getSchema().getType();
                if (type == null) type = "string"; // default fallback
                swagger2Param.put("type", type);

                if (param.getSchema().getFormat() != null) {
                    swagger2Param.put("format", param.getSchema().getFormat());
                }
                if (param.getSchema().getDefault() != null) {
                    swagger2Param.set("default", jsonMapper.valueToTree(param.getSchema().getDefault()));
                }
                if (param.getSchema().getMinimum() != null) {
                    swagger2Param.put("minimum", param.getSchema().getMinimum().doubleValue());
                }
                if (param.getSchema().getMaximum() != null) {
                    swagger2Param.put("maximum", param.getSchema().getMaximum().doubleValue());
                }
            }
        } else {
            // Missing schema, use string type as fallback
            swagger2Param.put("type", "string");
        }

        return swagger2Param;
    }

    private ObjectNode convertGlobalResponses(Map<String, ApiResponse> responses) {
        ObjectNode swagger2Responses = jsonMapper.createObjectNode();

        responses.forEach((name, response) -> {
            ObjectNode swagger2Response = jsonMapper.createObjectNode();

            String description = response.getDescription();
            if (description == null || description.trim().isEmpty()) {
                description = "Response " + name;
            }
            swagger2Response.put("description", description);

            if (response.getContent() != null && !response.getContent().isEmpty()) {
                response.getContent().values().stream().findFirst().ifPresent(mediaType -> {
                    if (mediaType.getSchema() != null) {
                        ObjectNode schema = convertSchemaToSwagger2(mediaType.getSchema());
                        swagger2Response.set("schema", schema);
                    }
                });
            }

            swagger2Responses.set(name, swagger2Response);
        });

        return swagger2Responses;
    }

    private ObjectNode convertSchemaToSwagger2(Schema<?> schema) {
        ObjectNode swagger2Schema = jsonMapper.createObjectNode();

        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            if (ref.startsWith("#/components/schemas/")) {
                ref = ref.replace("#/components/schemas/", "#/definitions/");
            }
            swagger2Schema.put("$ref", ref);
            return swagger2Schema;
        }

        if (schema.getType() != null) swagger2Schema.put("type", schema.getType());
        if (schema.getFormat() != null) swagger2Schema.put("format", schema.getFormat());
        if (schema.getDescription() != null) swagger2Schema.put("description", schema.getDescription());

        if (schema.getMinimum() != null) swagger2Schema.put("minimum", schema.getMinimum().doubleValue());
        if (schema.getMaximum() != null) swagger2Schema.put("maximum", schema.getMaximum().doubleValue());
        if (schema.getMinLength() != null) swagger2Schema.put("minLength", schema.getMinLength());
        if (schema.getMaxLength() != null) swagger2Schema.put("maxLength", schema.getMaxLength());
        if (schema.getPattern() != null) swagger2Schema.put("pattern", schema.getPattern());
        if (schema.getReadOnly() != null && schema.getReadOnly()) swagger2Schema.put("readOnly", true);

        if (schema.getRequired() != null && !schema.getRequired().isEmpty()) {
            ArrayNode required = jsonMapper.createArrayNode();
            schema.getRequired().forEach(required::add);
            swagger2Schema.set("required", required);
        }

        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            ObjectNode properties = jsonMapper.createObjectNode();
            schema.getProperties().forEach((propName, propSchema) -> {
                ObjectNode propNode = convertSchemaToSwagger2(propSchema);
                properties.set(propName, propNode);
            });
            swagger2Schema.set("properties", properties);
        }

        if (schema.getItems() != null) {
            ObjectNode items = convertSchemaToSwagger2(schema.getItems());
            swagger2Schema.set("items", items);
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            ArrayNode allOf = jsonMapper.createArrayNode();
            schema.getAllOf().forEach(subSchema -> {
                ObjectNode subNode = convertSchemaToSwagger2(subSchema);
                allOf.add(subNode);
            });
            swagger2Schema.set("allOf", allOf);
        }

        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            ArrayNode enumArray = jsonMapper.createArrayNode();
            schema.getEnum().forEach(value -> {
                enumArray.add(jsonMapper.valueToTree(value));
            });
            swagger2Schema.set("enum", enumArray);
        }

        if (schema.getDefault() != null) {
            swagger2Schema.set("default", jsonMapper.valueToTree(schema.getDefault()));
        }

        return swagger2Schema;
    }

    // Risoluzione riferimenti
    private Parameter resolveParameterReference(String ref) {
        if (currentOpenAPI.getComponents() == null || currentOpenAPI.getComponents().getParameters() == null) return null;
        String paramName = ref.substring(ref.lastIndexOf("/") + 1);
        return currentOpenAPI.getComponents().getParameters().get(paramName);
    }

    private ApiResponse resolveResponseReference(String ref) {
        if (currentOpenAPI.getComponents() == null || currentOpenAPI.getComponents().getResponses() == null) return null;
        String responseName = ref.substring(ref.lastIndexOf("/") + 1);
        return currentOpenAPI.getComponents().getResponses().get(responseName);
    }

    private Schema<?> resolveSchemaReference(String ref) {
        if (currentOpenAPI.getComponents() == null || currentOpenAPI.getComponents().getSchemas() == null) return null;
        String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
        return currentOpenAPI.getComponents().getSchemas().get(schemaName);
    }

    private String findMatchingGlobalParameter(Parameter param) {
        if (currentOpenAPI.getComponents() == null || currentOpenAPI.getComponents().getParameters() == null) {
            return null;
        }

        // Cerca un parametro globale che corrisponda esattamente a questo parametro
        for (Map.Entry<String, Parameter> entry : currentOpenAPI.getComponents().getParameters().entrySet()) {
            Parameter globalParam = entry.getValue();

            // Confronta tutte le proprietà principali del parametro
            if (parametersMatch(param, globalParam)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private boolean parametersMatch(Parameter param1, Parameter param2) {
        // Confronta nome, in, descrizione, required e schema
        if (!java.util.Objects.equals(param1.getName(), param2.getName())) return false;
        if (!java.util.Objects.equals(param1.getIn(), param2.getIn())) return false;
        if (!java.util.Objects.equals(param1.getDescription(), param2.getDescription())) return false;
        if (!java.util.Objects.equals(param1.getRequired(), param2.getRequired())) return false;

        // Confronta schema
        if (param1.getSchema() == null && param2.getSchema() == null) return true;
        if (param1.getSchema() == null || param2.getSchema() == null) return false;

        return schemasMatch(param1.getSchema(), param2.getSchema());
    }

    private boolean schemasMatch(Schema<?> schema1, Schema<?> schema2) {
        // Confronta le proprietà principali del schema
        if (!java.util.Objects.equals(schema1.getType(), schema2.getType())) return false;
        if (!java.util.Objects.equals(schema1.getFormat(), schema2.getFormat())) return false;
        if (!java.util.Objects.equals(schema1.get$ref(), schema2.get$ref())) return false;
        if (!java.util.Objects.equals(schema1.getDefault(), schema2.getDefault())) return false;
        if (!java.util.Objects.equals(schema1.getMinimum(), schema2.getMinimum())) return false;
        if (!java.util.Objects.equals(schema1.getMaximum(), schema2.getMaximum())) return false;

        return true;
    }
}