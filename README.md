# Quarkus Zanzibar

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.zanzibar/quarkus-zanzibar?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.zanzibar/quarkus-zanzibar)
[![Build](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-zanzibar/actions/workflows/build.yml)

## Overview

The **Quarkus Zanzibar** extension provides Zanzibar style Fine Grain Authorization (FGA) capabilities for Quarkus. An authorization filter and dedicated annotations are provided to provide easy integration of Zanzibar style FGA into applications.

The extension only provides the framework and relies on dedicated connectors to communicate with specific Zanzibar style
APIs.

Currently only [OpenFGA](https://openfga.dev) is supported.

## Usage

Adding the `quarkus-zanzibar` extension to your project provides access to the authorization and the annotations needed to configure authorization on your resource classes and methods.

To communicate with your selected API you will need to add a connector. For example, to connect to OpenFGA instances you would add the `quarkus-zanzibar-openfga` connector extension. This uses the [Quarkus OpenFGA Client](https://github.com/quarkiverse/quarkus-openfga-client) to provide connect Zanzibar to OpenFGA instances.

### Dependency

Add the `quarkus-zanzibar-openfga` extension to your Quarkus project in your `pom.xml` for Maven or `build.gradle(.kts)` for Gradle.

#### Maven

```xml
<dependency>
    <groupId>io.quarkiverse.zanzibar</groupId>
    <artifactId>quarkus-zanzibar-openfga</artifactId>
    <version>${zanzibar.version}</version>
</dependency>
```

#### Gradle

```kotlin
implementation("io.quarkiverse.zanzibar:quarkus-zanzibar-openfga:${zanzibar.version}")
```

### Resource Method Annotations

Zanzibar style authorization requires 4 distinct pieces of information: the object type & id, relation and the user. The extension provides resource method annotations to configure how the object and relation are determined from the current request and uses the standard authentication features of Quarkus to determine the user.

#### Object

The Zanzibar extension provides a number of annotations to determine the object type & id from the current request.

* `FGAPathObject`
  
  The object id is determined by a request path parameter.
  
* `FGAQueryObject`
  
  The object id is determined by a request query parameter.
  
* `FGAHeaderObject`

  The object id is determined by a request header value.
  
* `FGARequestObject`

  The object id is determined by a request property.
  
* `FGAObjcet`

  The object id is provided directly as a constant.
  

With all of these annotations the object type is provided as a constant separately.

While these annotations provide a large amount of options sometimes custom support will be needed; currently you can use `FGARequestObject` to achieve this. Any filter or other request interception method that runs before Zanzibar's authorization filer can store a property against the current request and then extract the value using the `FGARequestObject` annotation.

#### Relation

The relation is specified using a single annotation providing the relation as a constant.

* `FGARelation`

  The relation is provided as a constant.

#### Examples

To use the `id` from the request path to allow `reader`s access to a `thing`, you would annotate the method as follows:
```java
@FGAPathObject(param = "id", type = "thing")
@FGARelation("reader")
Thing getObject(@PathParam("id") String id) {
    ...
}
```

## Documentation

The documentation for this extension should be maintained as part of this repository and it is stored in the `docs/` directory.

The layout should follow the [Antora's Standard File and Directory Set](https://docs.antora.org/antora/2.3/standard-directories/).

Once the docs are ready to be published, please open a PR including this repository in the [Quarkiverse Docs Antora playbook](https://github.com/quarkiverse/quarkiverse-docs/blob/main/antora-playbook.yml#L7). See an example [here](https://github.com/quarkiverse/quarkiverse-docs/pull/1).
