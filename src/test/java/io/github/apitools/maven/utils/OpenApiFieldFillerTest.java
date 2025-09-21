package io.github.apitools.maven.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiFieldFillerTest {

    @Test
    void testFillMissingFieldsEmptyOpenAPI() {
        OpenAPI openAPI = new OpenAPI();

        OpenApiFieldFiller.fillMissingFields(openAPI);

        assertNotNull(openAPI.getInfo());
        assertEquals("Generated API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getPaths());
    }

    @Test
    void testFillMissingFieldsWithPartialInfo() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title("My API"));

        OpenApiFieldFiller.fillMissingFields(openAPI);

        assertEquals("My API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getPaths());
    }

    @Test
    void testFillMissingFieldsWithPartialVersion() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().version("2.0.0"));

        OpenApiFieldFiller.fillMissingFields(openAPI);

        assertEquals("Generated API", openAPI.getInfo().getTitle());
        assertEquals("2.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getPaths());
    }

    @Test
    void testFillMissingFieldsCompleteInfo() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title("Complete API").version("3.0.0"));
        openAPI.paths(new io.swagger.v3.oas.models.Paths());

        OpenApiFieldFiller.fillMissingFields(openAPI);

        assertEquals("Complete API", openAPI.getInfo().getTitle());
        assertEquals("3.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getPaths());
    }

    @Test
    void testFillMissingFieldsNullValues() {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setTitle(null);
        info.setVersion(null);
        openAPI.info(info);

        OpenApiFieldFiller.fillMissingFields(openAPI);

        assertEquals("Generated API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getPaths());
    }
}