# Table of contents

1. [Introduction](#introduction) provide a basic overview of JUI and the JUI project structure.
2. [Getting started](#getting-started) familiarises youself with JUI and JUI based application. 
3. [Building JUI](#building-jui) describes how to setup your environment to both build JUI and to develop with JUI.
4. [Contributing](#contributing) describes how you can contribute to the JUI project.

# Introduction

## Overview

JUI is a Java to JavaScript suite of libraries and tools for building web UI's in Java. It has its origins in [GWT](https://www.gwtproject.org/) however has been largely decoupled from this is it heads towards becoming a standalone platform. JUI does continue to employ the GWT compiler as it remains mature and relevant (with the intention of continuing to do so as long as is feasible; this is currently being mainted under the [GWT Project](https://github.com/gwtproject/gwt)).

## Documentation

Comprehensive usage documentation is provided (see [Documentation](https://juiproject.github.io/jui-stack/#/)) to guide you through learning JUI from a simple getting started tutorial through to detailed and targeted lessons.

The rest of this document outlines the structure of the overall JUI project itself

## Libraries

JUI consists of the following libraries:

1. [jui-platform](./jui-platform/) provides the core runtime for JUI code (including code needed to interface with the transpiler).
2. [jui-platform-codeserver](./jui-platform-codeserver/) is a debugging tool used during development.
3. [jui-core](./jui-core/) builds on `jui-platform` and provides a component framework with supporting functionality.
4. [jui-ui](./jui-ui/) builds on `jui-core` providing a minimal suite of core components (including controls).
5. [jui-remoting](./jui-remoting/) provides an RPC remoting framework (this is a bit historical and not a requirement for using JUI) along with server-side harness for handling queries and commands.
6. [jui-text](./jui-text/) provides a simple rich text format (that is serialiable) and an associated editor.
7. [jui-validation](./jui-validation/) a convenience to perform validation both client and server side in a portable manner.
8. [jui-test](./jui-test/) a collection of tools to perform testing of UI components and applications.
9. [jui-playground](./jui-playground/) delivers a component explorer to elucidate the various JUI concepts and core components and controls along with a number of examples (see [Playground](#playground) below).

These are all released against the same version (generated from this multi-module project).

## License

This code is licensed under [Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0). Dependencies are licensed separately and any code that is attributable is marked as such with any license exceptions made explicitly.

# Getting started

The best way to get started is to follow the [Getting started](https://juiproject.github.io/jui-stack/#/intro_gettingstarted) guide in the [Documentation](https://juiproject.github.io/jui-stack/). This will:

1. Ensure you have the required software installed.
2. Guide you through creating a simple JUI project.
3. Introduce you to some of the core JUI principles.
4. Provide a foundation for further experimentation and a place for you to implement the practical activities of the lessons.

As alluded to in (4) there are also a number of lessons that you can work through. These will expand on the core principles covered in the getting started guide, introduce and develop knowledge of components and provide practical guidance for specific topics (such as navigation and dialogs). It is recommended that you work through at least lessons 1 and 2.

## Playground

The [Getting started](https://juiproject.github.io/jui-stack/#/intro_gettingstarted) will walk you through the creation of a Spring Boot based JUI project that also provides a basis for the lesson practicums as well as for your own experimentation. However a JUI does come with an playground ([jui-playground](./jui-playground/)) project that includes solutions to the various lesson exercises as well as a variety of examples that make use of JUI components.

The playground can be run directly from the associated JAR, which can be installed directly from the Maven Central repository (replace `<version>` with the version you want to retrieve):

```bash
VERSION=<version>
mvn dependency:get -DgroupId=com.effacy.jui \
                   -DartifactId=jui-playground \
                   -Dversion=$VERSION \
                   -Dpackaging=jar
```

Once installed, open a terminal and run the following (again, replace `<version>` with the relevant version of JUI):

```bash
VERSION=<version>
java -jar ~/.m2/repository/com/effacy/jui/jui-playground/$VERSION/jui-playground-$VERSION.jar
```

This will start a Spring Boot application that when you point a browser to [localhost:8080](http://localhost:8080) you will access the **documentation** (for convenience) and to [localhost:8080/playground](http://localhost:8080/playground) you will access the **playground**.

Once you are done, the server can be stopped with `CTL+C`.

# Building JUI

*In general you should not need to build JUI from source (unless you are interested in [contributing](./CONTRIBUTING.md), or just interested).*

Before delving into the build process we make a few notes on the project structure itself.

First of all this is a Maven multimodule project that generates a number of libraries, along with the [playground](#playground) (see [libraries](#libraries)), under the same version. In addition to these modules there are some supporting assets (under [support](./support/)) and the source for the [product documentation](https://juiproject.github.io/jui-stack/).

This documentation is found under [docs](./docs/) and is constructed using [Docsify](https://docsify.js.org/) (along with [PlantUML](https://plantuml.com/guide) for UML generation and [Mermaid](https://mermaid.js.org/) for diagrams). If you have [python](https://www.python.org/) installed you can quite easily run up the documentation locally from source. To do so, open a terminal from the project root and run:

```bash
cd ./docs/
python3 -m http.server 3000
```

Pointing a browser to [http://localhost:3000/index.html#/](http://localhost:3000/index.html#/) will display the documentation.

Additional documentation (generally related to structure, process or specific design detail) is provided by separate readme files located in the relevant directories and packages.

## Build process

You will need the following to be installed to successfully perform a build:

1. Git
2. JDK (17 or greater)
3. Maven (3.8 or greater)
4. Chrome browser (optional, for running UI tests)

In all likelihood, if you have made it this far, you will have (at least some variant) the the above installed. If not there are installation guides provided in [CONTRIBUTING.MD](./CONTRIBUTING.md).

With these in place you can build a local version of JUI from a checked out version by running `mvn clean install` from the project root. If you haven't installed Chrome (in all likelihood it will be installed on your system already) then you will need to skip the tests with `mvn clean install -Dtests.skip=true`.

# Contributing

If your would like to contribute to the project then have a look at [CONTRIBUTING.MD](./CONTRIBUTING.md) for more details.


