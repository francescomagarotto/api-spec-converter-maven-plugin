# API Spec Converter Maven Plugin

Plugin Maven per convertire specifiche API tra OpenAPI 3.x e Swagger 2.0 (bidirezionale).

## Funzionalità

- Conversione bidirezionale tra OpenAPI 3.x e Swagger 2.0
- Output in formato JSON o YAML
- Validazione delle specifiche convertite
- Integrazione completa con il ciclo di vita Maven
- Gestione corretta dei riferimenti $ref nei parametri

## Utilizzo

### Configurazione nel pom.xml

```xml
<plugin>
    <groupId>io.github.apitools</groupId>
    <artifactId>api-spec-converter-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>convert</goal>
            </goals>
            <configuration>
                <source>src/main/resources/api.yaml</source>
                <from>swagger_2</from>
                <to>openapi_3</to>
                <syntax>yaml</syntax>
                <validate>true</validate>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Esecuzione da riga di comando

```bash
mvn api-spec-converter:convert \
  -Dsource=https://api.example.com/swagger.json \
  -Dfrom=swagger_2 \
  -Dto=openapi_3 \
  -Dsyntax=yaml \
  -Doutput=target/openapi.yaml
```

## Parametri

- `source`: File o URL della specifica sorgente (obbligatorio)
- `from`: Formato sorgente (obbligatorio)
- `to`: Formato destinazione (obbligatorio)
- `syntax`: Sintassi output (json/yaml, default: json)
- `order`: Ordinamento campi (openapi/alpha, default: openapi)
- `output`: File di output
- `outputDirectory`: Directory di output (default: target/generated-sources/api-spec)
- `validate`: Valida il risultato (default: false)
- `fillMissing`: Riempie campi mancanti (default: false)
- `skip`: Salta l'esecuzione (default: false)

## Build

```bash
mvn clean install
```

## Licenza

MIT License
