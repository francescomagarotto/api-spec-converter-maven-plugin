package io.github.apitools.maven.parsers;

import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerParserTest {

    @Mock
    private Log log;

    private SwaggerParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new SwaggerParser(log);
    }

    @Test
    void testParseOpenApi3() throws Exception {
        String openApi3Content = """
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

        ParsedSpec result = parser.parse(openApi3Content, SpecFormat.OPENAPI_3);

        assertEquals(SpecFormat.OPENAPI_3, result.getFormat());
        assertTrue(result.getData() instanceof OpenAPI);

        OpenAPI openAPI = (OpenAPI) result.getData();
        assertEquals("Test API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("3.0.1", openAPI.getOpenapi());
    }

    @Test
    void testParseSwagger2() throws Exception {
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

        ParsedSpec result = parser.parse(swagger2Content, SpecFormat.SWAGGER_2);

        assertEquals(SpecFormat.SWAGGER_2, result.getFormat());
        assertTrue(result.getData() instanceof OpenAPI);

        OpenAPI openAPI = (OpenAPI) result.getData();
        assertEquals("Swagger API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testParseJsonFormat() throws Exception {
        String jsonContent = """
            {
              "openapi": "3.0.1",
              "info": {
                "title": "JSON API",
                "version": "1.0.0"
              },
              "paths": {
                "/test": {
                  "get": {
                    "responses": {
                      "200": {
                        "description": "Success"
                      }
                    }
                  }
                }
              }
            }
            """;

        ParsedSpec result = parser.parse(jsonContent, SpecFormat.OPENAPI_3);

        assertEquals(SpecFormat.OPENAPI_3, result.getFormat());
        assertTrue(result.getData() instanceof OpenAPI);

        OpenAPI openAPI = (OpenAPI) result.getData();
        assertEquals("JSON API", openAPI.getInfo().getTitle());
    }

    @Test
    void testParseInvalidContent() {
        String invalidContent = "invalid yaml content: [}";

        Exception exception = assertThrows(Exception.class, () -> {
            parser.parse(invalidContent, SpecFormat.OPENAPI_3);
        });

        assertTrue(exception.getMessage().contains("Unable to parse") ||
                   exception instanceof com.fasterxml.jackson.core.JsonParseException);
    }

    @Test
    void testParseEmptyContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            parser.parse("", SpecFormat.OPENAPI_3);
        });

        assertTrue(exception.getMessage().contains("Unable to parse"));
    }

    @Test
    void testParseNullContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            parser.parse(null, SpecFormat.OPENAPI_3);
        });

        assertNotNull(exception);
    }

    @Test
    void testParseUnsupportedFormat() {
        // This should not happen with current enum but test defensive coding
        String content = "openapi: 3.0.1\ninfo:\n  title: Test\n  version: 1.0.0\npaths: {}";

        assertDoesNotThrow(() -> {
            parser.parse(content, SpecFormat.OPENAPI_3);
        });

        assertDoesNotThrow(() -> {
            parser.parse(content, SpecFormat.SWAGGER_2);
        });
    }

    @Test
    void testParseContentWithReferences() throws Exception {
        String contentWithRefs = """
            openapi: 3.0.1
            info:
              title: API with References
              version: 1.0.0
            paths:
              /users/{id}:
                parameters:
                  - $ref: '#/components/parameters/UserId'
                get:
                  responses:
                    '200':
                      $ref: '#/components/responses/UserResponse'
            components:
              parameters:
                UserId:
                  name: id
                  in: path
                  required: true
                  schema:
                    type: integer
              responses:
                UserResponse:
                  description: User data
                  content:
                    application/json:
                      schema:
                        type: object
            """;

        ParsedSpec result = parser.parse(contentWithRefs, SpecFormat.OPENAPI_3);

        assertEquals(SpecFormat.OPENAPI_3, result.getFormat());
        assertTrue(result.getData() instanceof OpenAPI);

        OpenAPI openAPI = (OpenAPI) result.getData();
        assertEquals("API with References", openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getComponents());
    }
}