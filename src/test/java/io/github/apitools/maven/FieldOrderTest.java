package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldOrderTest {

    @Test
    void testEnumValues() {
        assertEquals("openapi", FieldOrder.OPENAPI.getValue());
        assertEquals("alpha", FieldOrder.ALPHA.getValue());
    }

    @Test
    void testToString() {
        assertEquals("openapi", FieldOrder.OPENAPI.toString());
        assertEquals("alpha", FieldOrder.ALPHA.toString());
    }

    @Test
    void testFromString() {
        assertEquals(FieldOrder.OPENAPI, FieldOrder.fromString("openapi"));
        assertEquals(FieldOrder.ALPHA, FieldOrder.fromString("alpha"));
        assertEquals(FieldOrder.OPENAPI, FieldOrder.fromString("OPENAPI"));
        assertEquals(FieldOrder.ALPHA, FieldOrder.fromString("ALPHA"));
    }

    @Test
    void testFromStringInvalidOrder() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FieldOrder.fromString("invalid");
        });
        assertTrue(exception.getMessage().contains("Unrecognized field order"));
    }

    @Test
    void testIsSupported() {
        assertTrue(FieldOrder.isSupported("openapi"));
        assertTrue(FieldOrder.isSupported("alpha"));
        assertTrue(FieldOrder.isSupported("OPENAPI"));
        assertTrue(FieldOrder.isSupported("ALPHA"));
        assertFalse(FieldOrder.isSupported("invalid"));
        assertFalse(FieldOrder.isSupported(""));
        assertFalse(FieldOrder.isSupported(null));
    }
}