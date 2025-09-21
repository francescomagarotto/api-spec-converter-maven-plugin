package io.github.apitools.maven;

import io.swagger.v3.oas.models.OpenAPI;
import io.github.apitools.maven.converters.FromOpenApiConverter;
import io.github.apitools.maven.converters.ToOpenApiConverter;
import io.github.apitools.maven.parsers.SwaggerParser;
import io.github.apitools.maven.utils.OpenApiFieldFiller;
import io.github.apitools.maven.utils.OpenApiValidator;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Main converter that coordinates the various specialized implementations.
 */
public class ApiSpecConverter {

    private final Log log;
    private final HttpClient httpClient;

    // Parsers
    private final SwaggerParser swaggerParser;

    // Converters
    private final ToOpenApiConverter toOpenApiConverter;
    private final FromOpenApiConverter fromOpenApiConverter;

    public ApiSpecConverter(Log log) {
        this.log = log;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

        // Initialize parsers
        this.swaggerParser = new SwaggerParser(log);

        // Initialize converters
        this.toOpenApiConverter = new ToOpenApiConverter(log);
        this.fromOpenApiConverter = new FromOpenApiConverter(log);
    }

    /**
     * Converts an API specification from one format to another.
     */
    public ConversionResult convert(ConversionOptions options) throws Exception {
        log.info("Starting conversion from " + options.getFrom() + " to " + options.getTo());

        // Read source content
        String sourceContent = readSource(options.getSource());
        log.debug("Source content read: " + sourceContent.length() + " characters");

        // Parse source format
        ParsedSpec parsedSpec = parseSource(sourceContent, options.getFrom());

        // Convert to target format
        String convertedContent = convertToTarget(parsedSpec, options);

        // Validate if requested
        List<String> warnings = new ArrayList<>();
        if (options.isValidate()) {
            warnings.addAll(OpenApiValidator.validate(convertedContent, options.getTo()));
        }

        return new ConversionResult(convertedContent, warnings);
    }

    private String readSource(String source) throws IOException, InterruptedException {
        if (isUrl(source)) {
            return readFromUrl(source);
        } else {
            return readFromFile(source);
        }
    }

    private boolean isUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private String readFromUrl(String url) throws IOException, InterruptedException {
        log.info("Reading from URL: " + url);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error " + response.statusCode() +
                " while reading from " + url);
        }

        return response.body();
    }

    private String readFromFile(String filePath) throws IOException {
        log.info("Reading from file: " + filePath);

        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private ParsedSpec parseSource(String content, SpecFormat format) throws Exception {
        log.info("Parsing format: " + format);

        return switch (format) {
            case SWAGGER_2, OPENAPI_3 -> swaggerParser.parse(content, format);
        };
    }

    private String convertToTarget(ParsedSpec parsedSpec, ConversionOptions options) throws Exception {
        log.info("Converting to format: " + options.getTo());

        // Convert everything to OpenAPI first
        OpenAPI openAPI = toOpenApiConverter.convertToOpenAPI(parsedSpec);

        // Apply missing fields if requested
        if (options.isFillMissing()) {
            OpenApiFieldFiller.fillMissingFields(openAPI);
        }

        // Then convert from OpenAPI to target format
        return fromOpenApiConverter.convertFromOpenAPI(openAPI, options);
    }
}
