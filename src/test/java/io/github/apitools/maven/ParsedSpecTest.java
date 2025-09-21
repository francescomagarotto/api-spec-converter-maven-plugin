package io.github.apitools.maven;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParsedSpecTest {

    @Test
    void testParsedSpecCreation() {
        OpenAPI openAPI = new OpenAPI();
        ParsedSpec spec = new ParsedSpec(SpecFormat.OPENAPI_3, openAPI);

        assertEquals(SpecFormat.OPENAPI_3, spec.getFormat());
        assertEquals(openAPI, spec.getData());
    }

    @Test
    void testParsedSpecWithSwagger2() {
        String content = "swagger content";
        ParsedSpec spec = new ParsedSpec(SpecFormat.SWAGGER_2, content);

        assertEquals(SpecFormat.SWAGGER_2, spec.getFormat());
        assertEquals(content, spec.getData());
    }

    @Test
    void testParsedSpecWithNullData() {
        ParsedSpec spec = new ParsedSpec(SpecFormat.OPENAPI_3, null);

        assertEquals(SpecFormat.OPENAPI_3, spec.getFormat());
        assertNull(spec.getData());
    }
}