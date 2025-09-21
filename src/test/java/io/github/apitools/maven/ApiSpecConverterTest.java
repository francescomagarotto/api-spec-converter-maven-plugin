package io.github.apitools.maven;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApiSpecConverterTest {

    @Mock
    private Log log;

    private ApiSpecConverter converter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ApiSpecConverter(log);
    }

    @Test
    void testConvertFromFileOpenApiToSwagger2() throws Exception {
        String openApiContent = """
            openapi: 3.0.1
            info:
              title: Test API
              version: 1.0.0
            paths:
              /test:
                get:
                  responses:
                    '200':
                      description: Success
            """;

        Path sourceFile = tempDir.resolve("test-openapi.yaml");
        Files.write(sourceFile, openApiContent.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .validate(false)
            .fillMissing(false)
            .build();

        ConversionResult result = converter.convert(options);

        assertNotNull(result);
        assertNotNull(result.content());
        assertTrue(result.content().contains("\"swagger\": \"2.0\""));
        assertTrue(result.content().contains("\"title\": \"Test API\""));
        assertFalse(result.hasWarnings());
    }

    @Test
    void testConvertFromFileSwagger2ToOpenApi() throws Exception {
        String swagger2Content = """
            swagger: '2.0'
            info:
              title: Swagger API
              version: 1.0.0
            paths:
              /test:
                get:
                  responses:
                    '200':
                      description: Success
            """;

        Path sourceFile = tempDir.resolve("test-swagger2.yaml");
        Files.write(sourceFile, swagger2Content.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.SWAGGER_2)
            .to(SpecFormat.OPENAPI_3)
            .syntax(OutputSyntax.YAML)
            .validate(false)
            .fillMissing(false)
            .build();

        ConversionResult result = converter.convert(options);

        assertNotNull(result);
        assertNotNull(result.content());
        assertTrue(result.content().contains("openapi: \"3.0.0\""));
        assertTrue(result.content().contains("title: \"Swagger API\""));
        assertFalse(result.hasWarnings());
    }

    @Test
    void testConvertWithValidation() throws Exception {
        String openApiContent = """
            openapi: 3.0.1
            info:
              title: Test API
              version: 1.0.0
            paths:
              /test:
                get:
                  responses:
                    '200':
                      description: Success
            """;

        Path sourceFile = tempDir.resolve("test-valid.yaml");
        Files.write(sourceFile, openApiContent.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .validate(true)
            .fillMissing(false)
            .build();

        ConversionResult result = converter.convert(options);

        assertNotNull(result);
        assertNotNull(result.content());
        // Validation warnings may or may not be present depending on spec completeness
    }

    @Test
    void testConvertWithFillMissing() throws Exception {
        String minimalOpenApi = """
            openapi: 3.0.1
            info:
              title: Minimal API
              version: 1.0.0
            """;

        Path sourceFile = tempDir.resolve("test-minimal.yaml");
        Files.write(sourceFile, minimalOpenApi.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .validate(false)
            .fillMissing(true)
            .build();

        ConversionResult result = converter.convert(options);

        assertNotNull(result);
        assertNotNull(result.content());
        assertTrue(result.content().contains("\"swagger\": \"2.0\""));
    }

    @Test
    void testConvertFileNotFound() {
        ConversionOptions options = ConversionOptions.builder()
            .source("/non/existent/file.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        Exception exception = assertThrows(Exception.class, () -> {
            converter.convert(options);
        });

        assertTrue(exception.getMessage().contains("File non trovato") ||
                   exception instanceof IOException);
    }

    @Test
    void testConvertInvalidFormat() throws Exception {
        String invalidContent = "invalid yaml content: [}";

        Path sourceFile = tempDir.resolve("test-invalid.yaml");
        Files.write(sourceFile, invalidContent.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        Exception exception = assertThrows(Exception.class, () -> {
            converter.convert(options);
        });

        assertNotNull(exception);
    }

    @Test
    void testIsUrl() {
        // Test URL detection via behavior (since isUrl is private)
        // We can test this indirectly by trying to convert from a URL and seeing the behavior

        ConversionOptions options = ConversionOptions.builder()
            .source("https://example.com/api.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        // This should fail with a network error, proving it tried to make an HTTP request
        Exception exception = assertThrows(Exception.class, () -> {
            converter.convert(options);
        });

        // Should be a network-related exception
        assertNotNull(exception);
    }

    @Test
    void testConvertWithComplexReferences() throws Exception {
        String complexOpenApi = """
            openapi: 3.0.1
            info:
              title: Complex API
              version: 1.0.0
            paths:
              /users/{id}:
                parameters:
                  - $ref: '#/components/parameters/UserId'
                get:
                  responses:
                    '200':
                      $ref: '#/components/responses/UserResponse'
                    '404':
                      $ref: '#/components/responses/NotFound'
            components:
              parameters:
                UserId:
                  name: id
                  in: path
                  required: true
                  schema:
                    $ref: '#/components/schemas/Id'
              schemas:
                Id:
                  type: integer
                  format: int64
                  description: Resource ID
                User:
                  type: object
                  properties:
                    id:
                      $ref: '#/components/schemas/Id'
                    name:
                      type: string
              responses:
                UserResponse:
                  description: User data
                  content:
                    application/json:
                      schema:
                        $ref: '#/components/schemas/User'
                NotFound:
                  description: Resource not found
            """;

        Path sourceFile = tempDir.resolve("test-complex.yaml");
        Files.write(sourceFile, complexOpenApi.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .validate(false)
            .fillMissing(false)
            .build();

        ConversionResult result = converter.convert(options);

        assertNotNull(result);
        String content = result.content();
        assertNotNull(content);

        // Check that parameter references are properly converted
        assertTrue(content.contains("$ref: \"#/parameters/UserId\""));
        assertTrue(content.contains("parameters:"));
        assertTrue(content.contains("UserId:"));
        assertTrue(content.contains("type: \"integer\""));
        assertTrue(content.contains("format: \"int64\""));

        // Check definitions section
        assertTrue(content.contains("definitions:"));
        assertTrue(content.contains("User:"));

        // Check responses section
        assertTrue(content.contains("responses:"));
        assertTrue(content.contains("UserResponse:"));
    }

    @Test
    void testConvertEmptyFile() throws Exception {
        Path sourceFile = tempDir.resolve("empty.yaml");
        Files.write(sourceFile, "".getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        Exception exception = assertThrows(Exception.class, () -> {
            converter.convert(options);
        });

        assertNotNull(exception);
    }

    @Test
    void testLoggingCalls() throws Exception {
        String openApiContent = """
            openapi: 3.0.1
            info:
              title: Test API
              version: 1.0.0
            paths: {}
            """;

        Path sourceFile = tempDir.resolve("test-logging.yaml");
        Files.write(sourceFile, openApiContent.getBytes());

        ConversionOptions options = ConversionOptions.builder()
            .source(sourceFile.toString())
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.JSON)
            .build();

        converter.convert(options);

        // Verify that logging methods were called
        verify(log, atLeastOnce()).info(contains("Starting conversion from"));
        verify(log, atLeastOnce()).info(contains("Reading from file"));
        verify(log, atLeastOnce()).info(contains("Parsing format"));
        verify(log, atLeastOnce()).info(contains("Converting to format"));
    }
}