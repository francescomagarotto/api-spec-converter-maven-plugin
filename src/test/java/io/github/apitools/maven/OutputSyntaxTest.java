package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OutputSyntaxTest {

    @Test
    void testEnumValues() {
        assertEquals("json", OutputSyntax.JSON.getValue());
        assertEquals("yaml", OutputSyntax.YAML.getValue());
    }

    @Test
    void testToString() {
        assertEquals("json", OutputSyntax.JSON.toString());
        assertEquals("yaml", OutputSyntax.YAML.toString());
    }

    @Test
    void testFromString() {
        assertEquals(OutputSyntax.JSON, OutputSyntax.fromString("json"));
        assertEquals(OutputSyntax.YAML, OutputSyntax.fromString("yaml"));
        assertEquals(OutputSyntax.JSON, OutputSyntax.fromString("JSON"));
        assertEquals(OutputSyntax.YAML, OutputSyntax.fromString("YAML"));
    }

    @Test
    void testFromStringInvalidSyntax() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            OutputSyntax.fromString("xml");
        });
        assertTrue(exception.getMessage().contains("Unrecognized syntax"));
    }

    @Test
    void testIsSupported() {
        assertTrue(OutputSyntax.isSupported("json"));
        assertTrue(OutputSyntax.isSupported("yaml"));
        assertTrue(OutputSyntax.isSupported("JSON"));
        assertTrue(OutputSyntax.isSupported("YAML"));
        assertFalse(OutputSyntax.isSupported("xml"));
        assertFalse(OutputSyntax.isSupported(""));
        assertFalse(OutputSyntax.isSupported(null));
    }
}