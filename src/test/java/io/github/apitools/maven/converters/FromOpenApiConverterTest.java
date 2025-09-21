package io.github.apitools.maven.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.apitools.maven.ConversionOptions;
import io.github.apitools.maven.FieldOrder;
import io.github.apitools.maven.OutputSyntax;
import io.github.apitools.maven.SpecFormat;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FromOpenApiConverterTest {

    @Mock
    private Log log;

    private FromOpenApiConverter converter;
    private ObjectMapper jsonMapper;
    private YAMLMapper yamlMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new FromOpenApiConverter(log);
        jsonMapper = new ObjectMapper();
        yamlMapper = new YAMLMapper();
    }

    @Test
    void testConvertToSwagger2Json() throws Exception {
        OpenAPI openAPI = createBasicOpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        JsonNode jsonNode = jsonMapper.readTree(result);
        assertEquals("2.0", jsonNode.get("swagger").asText());
        assertEquals("Test API", jsonNode.get("info").get("title").asText());
        assertEquals("1.0.0", jsonNode.get("info").get("version").asText());
    }

    @Test
    void testConvertToSwagger2Yaml() throws Exception {
        OpenAPI openAPI = createBasicOpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        assertTrue(result.contains("swagger: \"2.0\""));
        assertTrue(result.contains("title: \"Test API\""));
        assertTrue(result.contains("version: \"1.0.0\""));
    }

    @Test
    void testConvertToOpenAPI3() throws Exception {
        OpenAPI openAPI = createBasicOpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.SWAGGER_2)
            .to(SpecFormat.OPENAPI_3)
            .syntax(OutputSyntax.JSON)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        JsonNode jsonNode = jsonMapper.readTree(result);
        assertEquals("3.0.0", jsonNode.get("openapi").asText());
        assertEquals("Test API", jsonNode.get("info").get("title").asText());
    }

    @Test
    void testConvertWithParameterReferences() throws Exception {
        OpenAPI openAPI = createOpenAPIWithParameterReferences();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        // Check that parameter references are correctly converted
        assertTrue(result.contains("$ref: \"#/parameters/Id\""));
        assertTrue(result.contains("parameters:"));
        assertTrue(result.contains("Id:"));
        assertTrue(result.contains("name: \"id\""));
        assertTrue(result.contains("in: \"path\""));
        assertTrue(result.contains("type: \"integer\""));
    }

    @Test
    void testConvertWithGlobalParametersOnly() throws Exception {
        OpenAPI openAPI = createOpenAPIWithGlobalParametersOnly();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        // Check that global parameters are properly defined
        assertTrue(result.contains("parameters:"));
        assertTrue(result.contains("UserId:"));
        assertTrue(result.contains("type: \"integer\""));
    }

    @Test
    void testConvertWithMissingInfo() throws Exception {
        OpenAPI openAPI = new OpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        JsonNode jsonNode = jsonMapper.readTree(result);
        assertEquals("2.0", jsonNode.get("swagger").asText());
        assertEquals("API", jsonNode.get("info").get("title").asText());
        assertEquals("1.0.0", jsonNode.get("info").get("version").asText());
    }

    @Test
    void testConvertUnsupportedFormat() {
        OpenAPI openAPI = createBasicOpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.OPENAPI_3) // Same format, should be handled by serializeOpenAPI
            .syntax(OutputSyntax.JSON)
            .build();

        assertDoesNotThrow(() -> {
            String result = converter.convertFromOpenAPI(openAPI, options);
            assertNotNull(result);
        });
    }

    @Test
    void testConvertWithComplexStructure() throws Exception {
        OpenAPI openAPI = createComplexOpenAPI();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        JsonNode jsonNode = jsonMapper.readTree(result);
        assertEquals("2.0", jsonNode.get("swagger").asText());

        // Check paths conversion
        assertTrue(jsonNode.has("paths"));
        assertTrue(jsonNode.get("paths").has("/users/{id}"));

        // Check definitions conversion
        assertTrue(jsonNode.has("definitions"));

        // Check responses conversion
        assertTrue(jsonNode.has("responses"));
    }

    @Test
    void testParameterWithSchemaReference() throws Exception {
        OpenAPI openAPI = createOpenAPIWithSchemaReferencedParameter();

        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .build();

        String result = converter.convertFromOpenAPI(openAPI, options);

        assertNotNull(result);
        // Schema references in parameters should be resolved to type information
        assertTrue(result.contains("type: \"integer\""));
        assertTrue(result.contains("format: \"int64\""));
    }

    private OpenAPI createBasicOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.openapi("3.0.0");
        openAPI.info(new Info().title("Test API").version("1.0.0"));
        openAPI.paths(new Paths());
        return openAPI;
    }

    private OpenAPI createOpenAPIWithParameterReferences() {
        OpenAPI openAPI = createBasicOpenAPI();

        // Add global parameter
        Components components = new Components();
        Map<String, Parameter> parameters = new HashMap<>();
        Parameter idParam = new PathParameter();
        idParam.setName("id");
        idParam.setRequired(true);
        idParam.setDescription("Resource ID");
        IntegerSchema schema = new IntegerSchema();
        schema.setFormat("int64");
        idParam.setSchema(schema);
        parameters.put("Id", idParam);
        components.setParameters(parameters);
        openAPI.setComponents(components);

        // Add path with parameter reference
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();

        // Add parameter that should match the global one
        Parameter operationParam = new PathParameter();
        operationParam.setName("id");
        operationParam.setRequired(true);
        operationParam.setDescription("Resource ID");
        IntegerSchema operationSchema = new IntegerSchema();
        operationSchema.setFormat("int64");
        operationParam.setSchema(operationSchema);
        operation.addParametersItem(operationParam);

        operation.setResponses(new ApiResponses().addApiResponse("200",
            new ApiResponse().description("Success")));

        pathItem.setGet(operation);
        paths.addPathItem("/items/{id}", pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }

    private OpenAPI createOpenAPIWithGlobalParametersOnly() {
        OpenAPI openAPI = createBasicOpenAPI();

        Components components = new Components();
        Map<String, Parameter> parameters = new HashMap<>();
        Parameter userIdParam = new PathParameter();
        userIdParam.setName("userId");
        userIdParam.setRequired(true);
        userIdParam.setDescription("User ID");
        userIdParam.setSchema(new IntegerSchema());
        parameters.put("UserId", userIdParam);
        components.setParameters(parameters);
        openAPI.setComponents(components);

        return openAPI;
    }

    private OpenAPI createComplexOpenAPI() {
        OpenAPI openAPI = createBasicOpenAPI();

        // Add components
        Components components = new Components();

        // Add schemas
        Map<String, Schema> schemas = new HashMap<>();
        Schema userSchema = new Schema();
        userSchema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        properties.put("id", new IntegerSchema());
        properties.put("name", new StringSchema());
        userSchema.setProperties(properties);
        schemas.put("User", userSchema);
        components.setSchemas(schemas);

        // Add responses
        Map<String, ApiResponse> responses = new HashMap<>();
        responses.put("NotFound", new ApiResponse().description("Not found"));
        components.setResponses(responses);

        openAPI.setComponents(components);

        // Add paths
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setResponses(new ApiResponses().addApiResponse("200",
            new ApiResponse().description("Success")));
        pathItem.setGet(operation);
        paths.addPathItem("/users/{id}", pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }

    private OpenAPI createOpenAPIWithSchemaReferencedParameter() {
        OpenAPI openAPI = createBasicOpenAPI();

        // Add schema
        Components components = new Components();
        Map<String, Schema> schemas = new HashMap<>();
        IntegerSchema idSchema = new IntegerSchema();
        idSchema.setFormat("int64");
        idSchema.setDescription("ID type");
        schemas.put("Id", idSchema);
        components.setSchemas(schemas);

        // Add parameter that references the schema
        Map<String, Parameter> parameters = new HashMap<>();
        Parameter param = new PathParameter();
        param.setName("id");
        param.setRequired(true);
        param.setDescription("Resource ID");
        Schema refSchema = new Schema();
        refSchema.set$ref("#/components/schemas/Id");
        param.setSchema(refSchema);
        parameters.put("IdParam", param);
        components.setParameters(parameters);

        openAPI.setComponents(components);

        return openAPI;
    }
}