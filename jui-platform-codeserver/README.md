# JUI Code Server

## Introduction

This is a (somewhat rewritten) re-constitution of the GWT code server but more narrowly focussed and rebranded so as to facilitate extension to support multuple compliation systems.

## Documentation

The bulk of the technical documentation for the code server resides in each of the packages:

1. [`com.effacy.jui.codeserver`](./src/main/java/com/effacy/jui/codeserver/) documentation for the coderserver framework and resource delivery.
2. [`com.effacy.jui.codeserver.gwt`](./src/main/java/com/effacy/jui/codeserver/gwt/) documentation for integration of the GWT compiler.
2. [`com.effacy.jui.codeserver.view`](./src/main/java/com/effacy/jui/codeserver/view/) documentation for view generation (the output delivered by the code server).

## Project structure and build

This is a standard Maven project with separate main and test source trees that generates a regular (thin) JAR. The code server is launched via the `codeserver` goal of the JUI Maven plugin (`jui-maven-plugin`), which resolves this JAR and its dependencies and runs `com.effacy.jui.codeserver.CodeServer` in a forked JVM (see [Code server](../docs/app_codeserver.md)).
