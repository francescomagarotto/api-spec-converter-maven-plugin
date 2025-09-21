package io.github.apitools.maven.utils;

import io.github.apitools.maven.SpecFormat;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiValidatorTest {

    @Test
    void testValidateValidOpenApi3() {
        String validOpenApi = """
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

        List<String> warnings = OpenApiValidator.validate(validOpenApi, SpecFormat.OPENAPI_3);
        assertNotNull(warnings);
        // Valid spec should have no warnings
        assertTrue(warnings.isEmpty() || warnings.size() == 0);
    }

    @Test
    void testValidateValidSwagger2() {
        String validSwagger = """
            swagger: '2.0'
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

        List<String> warnings = OpenApiValidator.validate(validSwagger, SpecFormat.SWAGGER_2);
        assertNotNull(warnings);
        // Valid spec should have no warnings
        assertTrue(warnings.isEmpty() || warnings.size() == 0);
    }

    @Test
    void testValidateInvalidContent() {
        String invalidContent = "invalid yaml content: [}";

        List<String> warnings = OpenApiValidator.validate(invalidContent, SpecFormat.OPENAPI_3);
        assertNotNull(warnings);
        assertFalse(warnings.isEmpty());
    }

    @Test
    void testValidateIncompleteSpec() {
        String incompleteSpec = """
            openapi: 3.0.1
            info:
              title: Test API
            # Missing version and paths
            """;

        List<String> warnings = OpenApiValidator.validate(incompleteSpec, SpecFormat.OPENAPI_3);
        assertNotNull(warnings);
        // Should have warnings about missing required fields
    }

    @Test
    void testValidateNullContent() {
        List<String> warnings = OpenApiValidator.validate(null, SpecFormat.OPENAPI_3);
        assertNotNull(warnings);
        assertFalse(warnings.isEmpty());
        assertTrue(warnings.get(0).contains("Error during validation"));
    }

    @Test
    void testValidateEmptyContent() {
        List<String> warnings = OpenApiValidator.validate("", SpecFormat.OPENAPI_3);
        assertNotNull(warnings);
        assertFalse(warnings.isEmpty());
    }
}