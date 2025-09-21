package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecFormatTest {

    @Test
    void testEnumValues() {
        assertEquals("swagger_2", SpecFormat.SWAGGER_2.getValue());
        assertEquals("openapi_3", SpecFormat.OPENAPI_3.getValue());
    }

    @Test
    void testToString() {
        assertEquals("swagger_2", SpecFormat.SWAGGER_2.toString());
        assertEquals("openapi_3", SpecFormat.OPENAPI_3.toString());
    }

    @Test
    void testFromString() {
        assertEquals(SpecFormat.SWAGGER_2, SpecFormat.fromString("swagger_2"));
        assertEquals(SpecFormat.OPENAPI_3, SpecFormat.fromString("openapi_3"));
        assertEquals(SpecFormat.SWAGGER_2, SpecFormat.fromString("SWAGGER_2"));
        assertEquals(SpecFormat.OPENAPI_3, SpecFormat.fromString("OPENAPI_3"));
    }

    @Test
    void testFromStringInvalidFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SpecFormat.fromString("invalid_format");
        });
        assertTrue(exception.getMessage().contains("Unrecognized format"));
    }

    @Test
    void testIsSupported() {
        assertTrue(SpecFormat.isSupported("swagger_2"));
        assertTrue(SpecFormat.isSupported("openapi_3"));
        assertTrue(SpecFormat.isSupported("SWAGGER_2"));
        assertTrue(SpecFormat.isSupported("OPENAPI_3"));
        assertFalse(SpecFormat.isSupported("invalid_format"));
        assertFalse(SpecFormat.isSupported("raml"));
        assertFalse(SpecFormat.isSupported(""));
        assertFalse(SpecFormat.isSupported(null));
    }

    @Test
    void testGetSupportedFormats() {
        String supported = SpecFormat.getSupportedFormats();
        assertTrue(supported.contains("swagger_2"));
        assertTrue(supported.contains("openapi_3"));
        assertTrue(supported.contains(", "));
    }
}