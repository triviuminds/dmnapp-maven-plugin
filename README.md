# dmnapp-maven-plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.dmnapp/dmnapp-maven-plugin.svg)](https://search.maven.org/artifact/com.dmnapp/dmnapp-maven-plugin)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A Maven plugin that automatically scans and processes Decision Model and Notation (DMN) files in your project. It extracts metadata from DMN files and generates a comprehensive JSON dmnapp file (`dmnapp.json`).

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Usage](#usage)
- [Output Format](#output-format)
- [License](#license)

## Features

- 🔍 Automatic scanning of project resources for DMN files
- 📝 Extraction of DMN metadata (name, namespace, relative path)
- 📊 Generation of structured JSON dmnapp file


## Requirements

- Java 17 or higher
- Maven 3.9.x or higher
- Project containing DMN files

## Usage

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.triviuminds.dmn</groupId>
            <artifactId>dmnapp-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
              <execution>
                <id>dmnapp-tweaks</id>
                <phase>post-integration-test</phase>
                <goals>
                  <goal>generate-dmnapp</goal>
                </goals>
              </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Output

The plugin generates a JSON file with the following structure:

```json
{
  "name" : "sample-dmnapp",
  "version" : "1.0.0",
  "dmnSpec" : "1.5",
  "apiSpec" : "openapi.json",
  "dmns" : [ 
    {"name" : "sum", "namespace" : "https://kiegroup.org/dmn/_D5C09393-48EC-41FB-AA30-ED6C0D2F5987", "relativePath" : "/"},
    {"name" : "messages", "namespace" : "com.triviuminds.dmn", "relativePath" : "/com/triviuminds/dmn"}
  ]
}
```

## License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.