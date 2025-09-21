# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven plugin for bidirectional conversion between OpenAPI 3.x and Swagger 2.0 specifications.

## Development Commands

### Build and Test
- `mvn clean install` - Full build with tests
- `mvn clean compile` - Compile only
- `mvn test` - Run tests using JUnit 5
- `mvn surefire:test` - Run specific tests

### Plugin Commands
- `mvn api-spec-converter:convert` - Run the conversion goal
- `mvn api-spec-converter:help` - Show plugin help

### Testing the Plugin
- Use command line parameters: `mvn api-spec-converter:convert -Dsource=test.yaml -Dfrom=openapi_3 -Dto=swagger_2 -Dsyntax=json`
- Test file available: `test.yaml` in project root

## Architecture

### Core Components

**ConvertMojo** (`src/main/java/io/github/apitools/maven/ConvertMojo.java`)
- Main Maven plugin goal implementation
- Handles parameter validation and orchestrates conversion
- Supports both file and URL sources

**ApiSpecConverter** (`src/main/java/io/github/apitools/maven/ApiSpecConverter.java`)
- Central converter that coordinates parsing and conversion
- Uses HTTP client for URL-based sources
- Simplified to only handle OpenAPI 3 and Swagger 2

**Parsers Package** (`src/main/java/io/github/apitools/maven/parsers/`)
- SwaggerParser: Handles both OpenAPI 3 and Swagger 2 parsing

**Converters Package** (`src/main/java/io/github/apitools/maven/converters/`)
- ToOpenApiConverter: Simplified to only handle already-parsed OpenAPI objects
- FromOpenApiConverter: Converts OpenAPI to Swagger 2 with proper $ref handling

**Utils Package** (`src/main/java/io/github/apitools/maven/utils/`)
- OpenApiValidator: Validates converted specifications
- OpenApiFieldFiller: Fills missing required fields

### Configuration Classes
- **SpecFormat**: Enum for supported formats (swagger_2, openapi_3)
- **OutputSyntax**: JSON or YAML output
- **FieldOrder**: Field ordering (openapi, alpha)
- **ConversionOptions**: Builder pattern for conversion parameters

### Key Dependencies
- Swagger Parser v3 for OpenAPI/Swagger handling
- Jackson for JSON/YAML processing
- Maven Plugin API (Java 17 target)

### Testing
- Uses JUnit 5 and Mockito
- Maven Plugin Testing Harness for integration tests
- Test class: `ConvertMojoTest.java`

## Working with the Code

### Parameter $ref Handling
The plugin correctly handles parameter references when converting from OpenAPI 3 to Swagger 2:
- Operation parameters that reference global parameters use `$ref: "#/parameters/Id"`
- Global parameter definitions contain full parameter specifications
- Schema references within parameters are resolved to type information

### Plugin Parameters
All parameters support both Maven configuration and command-line properties:
- source: Input file path or URL (required)
- from/to: Source/target formats (swagger_2, openapi_3) (required)
- syntax: json/yaml output (default: json)
- order: openapi/alpha field ordering (default: openapi)
- output: Specific output file path
- outputDirectory: Output directory (default: target/generated-sources/api-spec)
- validate: Validate result (default: false)
- fillMissing: Fill missing fields (default: false)
- skip: Skip execution (default: false)