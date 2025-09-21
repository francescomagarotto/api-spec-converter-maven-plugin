package io.github.apitools.maven.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public class OpenApiFieldFiller {

    public static void fillMissingFields(OpenAPI openAPI) {
        if (openAPI.getInfo() == null) {
            openAPI.info(new Info()
                .title("Generated API")
                .version("1.0.0"));
        }

        if (openAPI.getInfo().getTitle() == null) {
            openAPI.getInfo().setTitle("Generated API");
        }

        if (openAPI.getInfo().getVersion() == null) {
            openAPI.getInfo().setVersion("1.0.0");
        }

        if (openAPI.getPaths() == null) {
            openAPI.paths(new io.swagger.v3.oas.models.Paths());
        }
    }
}
