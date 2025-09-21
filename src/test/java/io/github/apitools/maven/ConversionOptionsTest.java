package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConversionOptionsTest {

    @Test
    void testBuilderPattern() {
        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .syntax(OutputSyntax.YAML)
            .order(FieldOrder.ALPHA)
            .validate(true)
            .fillMissing(true)
            .build();

        assertEquals("test.yaml", options.getSource());
        assertEquals(SpecFormat.OPENAPI_3, options.getFrom());
        assertEquals(SpecFormat.SWAGGER_2, options.getTo());
        assertEquals(OutputSyntax.YAML, options.getSyntax());
        assertEquals(FieldOrder.ALPHA, options.getOrder());
        assertTrue(options.isValidate());
        assertTrue(options.isFillMissing());
    }

    @Test
    void testBuilderDefaults() {
        ConversionOptions options = ConversionOptions.builder()
            .source("test.yaml")
            .from(SpecFormat.OPENAPI_3)
            .to(SpecFormat.SWAGGER_2)
            .build();

        assertEquals("test.yaml", options.getSource());
        assertEquals(SpecFormat.OPENAPI_3, options.getFrom());
        assertEquals(SpecFormat.SWAGGER_2, options.getTo());
        assertNotNull(options.getSyntax());
        assertNotNull(options.getOrder());
        assertFalse(options.isValidate());
        assertFalse(options.isFillMissing());
    }

    @Test
    void testAllCombinations() {
        // Test JSON syntax
        ConversionOptions jsonOptions = ConversionOptions.builder()
            .source("test.json")
            .from(SpecFormat.SWAGGER_2)
            .to(SpecFormat.OPENAPI_3)
            .syntax(OutputSyntax.JSON)
            .order(FieldOrder.OPENAPI)
            .validate(false)
            .fillMissing(false)
            .build();

        assertEquals(OutputSyntax.JSON, jsonOptions.getSyntax());
        assertEquals(FieldOrder.OPENAPI, jsonOptions.getOrder());
        assertFalse(jsonOptions.isValidate());
        assertFalse(jsonOptions.isFillMissing());
    }
}