package io.github.apitools.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Goal that converts API specifications between different formats.
 */
@Mojo(name = "convert", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ConvertMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "source", required = true)
    private String source;

    @Parameter(property = "from", required = true)
    private String from;

    @Parameter(property = "to", required = true)
    private String to;

    @Parameter(property = "syntax", defaultValue = "json")
    private String syntax;

    @Parameter(property = "order", defaultValue = "openapi")
    private String order;

    @Parameter(property = "output")
    private String output;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/api-spec")
    private File outputDirectory;

    @Parameter(property = "validate", defaultValue = "false")
    private boolean validate;

    @Parameter(property = "fillMissing", defaultValue = "false")
    private boolean fillMissing;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("API spec conversion skipped.");
            return;
        }

        getLog().info("Starting conversion from " + from + " to " + to);
        getLog().info("Source file: " + source);

        try {
            validateParameters();
            ApiSpecConverter converter = new ApiSpecConverter(getLog());

            if (output == null && outputDirectory != null) {
                outputDirectory.mkdirs();
            }

            ConversionResult result = converter.convert(
                ConversionOptions.builder()
                    .source(source)
                    .from(SpecFormat.fromString(from))
                    .to(SpecFormat.fromString(to))
                    .syntax(OutputSyntax.fromString(syntax))
                    .order(FieldOrder.fromString(order))
                    .validate(validate)
                    .fillMissing(fillMissing)
                    .build()
            );

            writeOutput(result);
            getLog().info("Conversion completed successfully!");

            if (result.hasWarnings()) {
                for (String warning : result.warnings()) {
                    getLog().warn(warning);
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Error during API specification conversion", e);
        }
    }

    private void validateParameters() throws MojoExecutionException {
        if (source == null || source.trim().isEmpty()) {
            throw new MojoExecutionException("The 'source' parameter is required");
        }

        if (!SpecFormat.isSupported(from)) {
            throw new MojoExecutionException("Unsupported source format: " + from +
                ". Supported formats: " + SpecFormat.getSupportedFormats());
        }

        if (!SpecFormat.isSupported(to)) {
            throw new MojoExecutionException("Unsupported target format: " + to +
                ". Supported formats: " + SpecFormat.getSupportedFormats());
        }

        if (!OutputSyntax.isSupported(syntax)) {
            throw new MojoExecutionException("Unsupported output syntax: " + syntax +
                ". Supported syntaxes: json, yaml");
        }

        if (!FieldOrder.isSupported(order)) {
            throw new MojoExecutionException("Unsupported field order: " + order +
                ". Supported orders: openapi, alpha");
        }
    }

    private void writeOutput(ConversionResult result) throws IOException {
        String content = result.content();
        
        if (output != null) {
            Path outputPath = Paths.get(output);
            if (!outputPath.isAbsolute()) {
                outputPath = project.getBasedir().toPath().resolve(outputPath);
            }
            
            Path parentDir = outputPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.write(outputPath, content.getBytes(StandardCharsets.UTF_8));
            
            getLog().info("Output written to: " + outputPath.toAbsolutePath());
        } else if (outputDirectory != null) {
            String fileName = generateFileName();
            Path outputPath = outputDirectory.toPath().resolve(fileName);
            
            Files.write(outputPath, content.getBytes(StandardCharsets.UTF_8));
            getLog().info("Output written to: " + outputPath.toAbsolutePath());
        } else {
            System.out.println(content);
        }
    }

    private String generateFileName() {
        String baseName = extractBaseName(source);
        String extension = syntax.equals("yaml") ? "yaml" : "json";
        return baseName + "_" + to + "." + extension;
    }

    private String extractBaseName(String source) {
        String fileName = source;
        if (source.contains("/")) {
            fileName = source.substring(source.lastIndexOf("/") + 1);
        }
        if (source.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }
        
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }
        
        return fileName.isEmpty() ? "converted_spec" : fileName;
    }
}
