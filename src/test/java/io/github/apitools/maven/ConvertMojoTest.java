package io.github.apitools.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConvertMojoTest {

    @Test
    public void testSpecFormatValidation() {
        assertTrue(SpecFormat.isSupported("openapi_3"));
        assertTrue(SpecFormat.isSupported("swagger_2"));
        assertFalse(SpecFormat.isSupported("invalid_format"));
    }

    @Test
    public void testOutputSyntaxValidation() {
        assertTrue(OutputSyntax.isSupported("json"));
        assertTrue(OutputSyntax.isSupported("yaml"));
        assertFalse(OutputSyntax.isSupported("xml"));
    }
}
