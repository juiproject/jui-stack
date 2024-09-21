# JUI Code Server

## Introduction

TBD

## Documentation

The bulk of the technical documentation for the code server resides in each of the packages:

1. [`com.effacy.jui.codeserver`](./src/main/java/com/effacy/jui/codeserver/README.md) documentation for the coderserver framework and resource delivery.
2. [`com.effacy.jui.codeserver.gwt`](./src/main/java/com/effacy/jui/codeserver/gwt/README.md) documentation for integration of the GWT compiler.
2. [`com.effacy.jui.codeserver.view`](./src/main/java/com/effacy/jui/codeserver/view/README.md) documentation for view generation (the output delivered by the code server).

## Project structure and build

This is a standard Maven project with separate main and test source trees. However, rather than generating a single artefact it generates two:

1. `com.effacy.jui:jui-platform-codeserver:jar` this is an executable JAR that runs the code server (a Spring Boot application). It includes all dependencies required to run, however the JUI build sources (and for rebind, class files) need to be available on the classpath. Configuration options are passed as normal.
2. `com.effacy.jui:jui-platform-codeserver:jar-with-dependencies` this is a standard JAR file however includes all the dependencies fully expanded. This can be included standalone to run the code server from the IDE as a run configuration (as supported by the IDE).

Note that both of these files are quite large as they repackage the dependencies.