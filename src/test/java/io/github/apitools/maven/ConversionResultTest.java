package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ConversionResultTest {

    @Test
    void testConversionResultWithWarnings() {
        ConversionResult result = new ConversionResult("converted content",
            Arrays.asList("warning1", "warning2"));

        assertEquals("converted content", result.content());
        assertTrue(result.hasWarnings());
        assertEquals(2, result.warnings().size());
        assertTrue(result.warnings().contains("warning1"));
        assertTrue(result.warnings().contains("warning2"));
    }

    @Test
    void testConversionResultWithoutWarnings() {
        ConversionResult result = new ConversionResult("converted content",
            Collections.emptyList());

        assertEquals("converted content", result.content());
        assertFalse(result.hasWarnings());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void testConversionResultWithNullWarnings() {
        ConversionResult result = new ConversionResult("converted content", null);

        assertEquals("converted content", result.content());
        assertFalse(result.hasWarnings());
        assertNotNull(result.warnings());
        assertTrue(result.warnings().isEmpty());
    }
}