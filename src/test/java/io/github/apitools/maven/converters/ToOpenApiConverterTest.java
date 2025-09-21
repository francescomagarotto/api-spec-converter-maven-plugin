package io.github.apitools.maven.converters;

import io.github.apitools.maven.ParsedSpec;
import io.github.apitools.maven.SpecFormat;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class ToOpenApiConverterTest {

    @Mock
    private Log log;

    private ToOpenApiConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ToOpenApiConverter(log);
    }

    @Test
    void testConvertOpenAPI3() {
        OpenAPI originalOpenAPI = new OpenAPI();
        originalOpenAPI.info(new Info().title("Test API").version("1.0.0"));

        ParsedSpec parsedSpec = new ParsedSpec(SpecFormat.OPENAPI_3, originalOpenAPI);

        OpenAPI result = converter.convertToOpenAPI(parsedSpec);

        assertSame(originalOpenAPI, result);
        assertEquals("Test API", result.getInfo().getTitle());
        assertEquals("1.0.0", result.getInfo().getVersion());
    }

    @Test
    void testConvertSwagger2() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title("Swagger API").version("2.0.0"));

        ParsedSpec parsedSpec = new ParsedSpec(SpecFormat.SWAGGER_2, openAPI);

        OpenAPI result = converter.convertToOpenAPI(parsedSpec);

        assertSame(openAPI, result);
        assertEquals("Swagger API", result.getInfo().getTitle());
        assertEquals("2.0.0", result.getInfo().getVersion());
    }

    @Test
    void testConvertNonOpenAPIData() {
        String stringData = "some string data";
        ParsedSpec parsedSpec = new ParsedSpec(SpecFormat.OPENAPI_3, stringData);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToOpenAPI(parsedSpec);
        });

        assertTrue(exception.getMessage().contains("Conversion not supported from"));
    }

    @Test
    void testConvertNullData() {
        ParsedSpec parsedSpec = new ParsedSpec(SpecFormat.OPENAPI_3, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToOpenAPI(parsedSpec);
        });

        assertTrue(exception.getMessage().contains("Conversion not supported from"));
    }

    @Test
    void testConstructorWithLog() {
        ToOpenApiConverter newConverter = new ToOpenApiConverter(log);
        assertNotNull(newConverter);

        // Test that it can still process data correctly
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title("Test").version("1.0.0"));
        ParsedSpec parsedSpec = new ParsedSpec(SpecFormat.OPENAPI_3, openAPI);

        OpenAPI result = newConverter.convertToOpenAPI(parsedSpec);
        assertSame(openAPI, result);
    }
}