# Table of contents

1. [Introduction](#introduction) provide a basic overview of JUI and the JUI project structure.
2. [Getting started](#getting-started) familiarises youself with JUI and JUI based application. 
3. [Environment](#environment) describes how to setup your environment to both build JUI and to develop with JUI.
4. [Contributing](#contributing) describes how you can contribute to the JUI project.

# Introduction

## Overview

JUI is a Java to JavaScript suite of libraries and tools for building web UI's in Java. It has its origins in [GWT](https://www.gwtproject.org/) however has been largely decoupled from this is it heads towards becoming a standalone platform. JUI does continue to employ the GWT compiler as it remains mature and relevant (with the intention of continuing to do so as long as is feasible; this is currently being mainted under the [GWT Project](https://github.com/gwtproject/gwt)).

Comprehensive documentation is provided (see [Documentation](#documentation)) to guide you through learning JUI from a simple getting started tutorial through to detailed and targeted lessons.

## Libraries

The primary JUI libraries consist of:

1. [jui-platform](./jui-platform/) provides the core runtime for JUI code (including code needed to interface with the transpiler).
2. [jui-platform-codeserver](./jui-platform-codeserver/) is a debugging tool used during development.
3. [jui-core](./jui-core/) builds on `jui-platform` and provides a component framework with supporting functionality.
4. [jui-ui](./jui-ui/) builds on `jui-core` providing a minimal suite of core components (including controls).
5. [jui-remoting](./jui-remoting/) provides an RPC remoting framework (this is a bit historical and not a requirement for using JUI) along with server-side harness for handling queries and commands.
6. [jui-text](./jui-text/) provides a simple rich text format (that is serialiable) and an associated editor.
7. [jui-validation](./jui-validation/) a convenience to perform validation both client and server side in a portable manner.
8. [jui-test](./jui-test/) a collection of tools to perform testing of UI components and applications.
9. [jui-playground](./jui-playground/) delivers a component explorer to elucidate the various JUI concepts and core components and controls along with a number of examples.

These are all generated under the same version from this multi-module Maven project.

## License

This code is licensed under [Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0). Dependencies are licensed separately and any code that is attributable is marked as such with any license exceptions made explicitly.

# Getting started

***Important**: Currently this project is pre-public access so it is assumed that you have checked the project out somewhere suitable in order to perform a local build and install (see [Environment: Building JUI](#building-jui)) and to access the documentation.*

The best way to get started is to follow the **Getting started** guide in the [Documentation](#documentation). This will:

1. Ensure you have the required software installed.
2. Guide you through creating a simple JUI project.
3. Introduce you to some of the core JUI principles.
4. Provide a foundation for further experimentation and a place for you to implement the practical activities of the lessons.

As alluded to in (4) there are also a number of lessons that you can work through. These will expand on the core principles covered in the getting started guide, introduce and develop knowledge of components and provide practical guidance for specific topics (such as navigation and dialogs). It is recommended that you work through at least lessons 1 and 2.

Obviously you need to gain access to the documentation, that is described in the following section.

## Documentation

The documentation resides under [docs](./docs/) and is constructed using [Docsify](https://docsify.js.org/) (along with [PlantUML](https://plantuml.com/guide) for UML generation and [Mermaid](https://mermaid.js.org/) for diagrams). If you have [python](https://www.python.org/) installed you can quite easily run up the documentation directly from source. To do so, open a terminal from the project root and run:

```bash
cd ./docs/
python3 -m http.server 3000
```

Pointing a browser to [http://localhost:3000/index.html#/](http://localhost:3000/index.html#/) will display the documentation. Note that you can effect the same by running the script `support/docs`.

It should be quite apparent where to find the *getting started* guide along with the various *lessons*.

*If you don't have python installed then you can access the documentation by running the playground, as described in the next section.*

## Playground

The **playground** itself provides documentation for the standard JUI components presented as a *component library* (and one that you can make use of for your own components) as well as a demonstration of the various capabilities of JUI and sample solutions to the various lessons. As such it is designed as a tool of pedagogy and for this reason it bundles in the [documentation](#documentation) as a matter of convenience.

In terms of running the playground the most practical way of doing so is from the self-executable `jui-playground` jar generated from the [jui-playground](./jui-playground/) module.

To do so you need to first ensure you have the jar installed (see [Environment: Building JUI](#building-jui)) in your local Maven repository. Having assured this, open a terminal and run the following (again, replace `<version>` with the relevant version of JUI):

```bash
VERSION=<version>
java -jar ~/.m2/repository/com/effacy/jui/jui-playground/$VERSION/jui-playground-$VERSION.jar
```

This will start a Spring Boot application that when you point a browser to [localhost:8080](http://localhost:8080) you will access the **documentation** and to [localhost:8080/playground](http://localhost:8080/playground) you will access the **playground**.

Once you are done, the server can be stopped with `CTL+C`.

*If you want to change the port, to 3000 for example, then use the switch `-Dserver.port=3000`.*

For reference, once the project is public access, one can download the JAR from the central Maven repository with:

```bash
VERSION=<version>
mvn dependency:get -DgroupId=com.effacy.jui -DartifactId=jui-playground -Dversion=$VERSION \
                   -Dtransitive=false
```

replacing `<version>` with the relevant version of JUI.

# Environment

## Building JUI

At a minimum you will require the following to be installed to successfully perform a build:

1. Git
2. JDK (17 or greater)
3. Maven (3.8 or greater)
4. Chrome browser (optional, for running UI tests)

In all likelihood, if you have made it this far, you will have (at least some variant) the the above installed. If not there are installation guides provided in [CONTRIBUTING.MD](./CONTRIBUTING.md).

With these in place you can build a local version of JUI from a checked out version by running `mvn clean install` from the project root. If you haven't installed Chrome (in all likelihood it will be installed on your system already) then you will need to skip the tests with `mvn clean install -Dtests.skip=true`.

## Using JUI

To develop a JUI application you will need a suitably configured IDE. See the **getting started** guide in the **documentation hub** for a more comprehensive description of the required setup (described in the appendix of the guide).

# Contributing

If you which to develop against the platform see [CONTRIBUTING.MD](./CONTRIBUTING.md) for details on how to setup your IDE and import the project.


