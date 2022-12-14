# Quarkus Zanzibar

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.zanzibar/quarkus-zanzibar?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.zanzibar/quarkus-zanzibar)
[![Build](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml)

## Overview

The **Quarkus Zanzibar** extension provides Zanzibar style Fine Grain Authorization (FGA) capabilities for Quarkus. An authorization filter and dedicated annotations are provided to provide easy integration of Zanzibar style FGA into applications.

The extension only provides the framework and relies on dedicated connectors to communicate with specific Zanzibar style
APIs.

Supported APIs:

- [OpenFGA](https://openfga.dev)
- [Authzed](https://authzed.com)

## Documentation

The documentation for this extension can be found
[here](https://quarkiverse.github.io/quarkiverse-docs/quarkus-zanzibar/dev/index.html).

## Dependency

Adding the `quarkus-zanzibar` extension to your project only provides access to the authorization and the annotations
needed to configure authorization on your resource classes and methods.

To communicate with your selected API you will need to add a connector for OpenFGA or Authzed.

### Maven
OpenFGA Zanzibar Connector
```xml
<dependency>
    <groupId>io.quarkiverse.zanzibar</groupId>
    <artifactId>quarkus-zanzibar-openfga</artifactId>
    <version>${zanzibar.version}</version>
</dependency>
```

Authzed Zanzibar Connector
```xml
<dependency>
    <groupId>io.quarkiverse.zanzibar</groupId>
    <artifactId>quarkus-zanzibar-authzed</artifactId>
    <version>${zanzibar.version}</version>
</dependency>
```

### Gradle

OpenFGA Zanzibar Connector
```kotlin
implementation("io.quarkiverse.zanzibar:quarkus-zanzibar-openfga:${zanzibar.version}")
```

Authzed Zanzibar Connector
```kotlin
implementation("io.quarkiverse.zanzibar:quarkus-zanzibar-authzed:${zanzibar.version}")
```
