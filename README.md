# Quarkus Zanzibar
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.zanzibar/quarkus-zanzibar?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.zanzibar/quarkus-zanzibar)
[![Build](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml)

## Overview

The **Quarkus Zanzibar** extension provides Zanzibar style Fine Grain Authorization (FGA) capabilities for Quarkus.
Authorization is integrated with Quarkus' standard `@PermissionsAllowed` mechanism using `ZanzibarPermission` implementations.

The extension only provides the framework and relies on dedicated connectors to communicate with specific Zanzibar style
APIs.

Supported APIs:

- [OpenFGA](https://openfga.dev)
- [Authzed](https://authzed.com)

## Documentation

The documentation for this extension can be found
[here](https://quarkiverse.github.io/quarkiverse-docs/quarkus-zanzibar/dev/index.html).

## Integration Tests

Connector-agnostic integration tests live in the `integration-tests` module. They are executed twice during
`mvn clean install` (OpenFGA + Authzed). To run them explicitly for each connector:

- `mvn -pl integration-tests -Popenfga -Dquarkus.profile=openfga -DskipCommonITs=false test`
- `mvn -pl integration-tests -Pauthzed,!openfga -Dquarkus.profile=authzed -DskipCommonITs=false test`

## Dependency

Adding the `quarkus-zanzibar` extension to your project provides the runtime authorization integration and APIs
needed to configure permission checks on your resource classes and methods.

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

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kdubb"><img src="https://avatars.githubusercontent.com/u/787655?v=4?s=100" width="100px;" alt="Kevin Wooten"/><br /><sub><b>Kevin Wooten</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-zanzibar/commits?author=kdubb" title="Code">💻</a> <a href="#maintenance-kdubb" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
