
# Overview

This project provides a means to explore various JUI concepts and components as well as test changes you make to those (or new) components. In particular this includes:

1. An exemplar application with various examples of components.
2. A tutorial to help you orient yourself to developing components (from visual design to implementation). This is documented in the documentation hub (see next item).
3. A documentation hub that provides a reference to the concepts and specific components.

The playground is intended to be run locally in your IDE (which allows for modification) and is deployed as a Spring Boot application.

**This document describes how to get going with the playground.**

# Running the application

It is assumed that you are running this from your IDE.

## Application

To run the application you must first build the project to create the bootstrap artefacts needed by the application to initialise JUI.

```bash
mvn clean install -Dtests.skip=true
```

Note that we don't need to run the tests for this purpose. Once the application has been build you can launch it from your IDE.

### Launch from VS Code

Add the following to your `launch.json` file:

```json
{
    "type": "java",
    "name": "JUI PlaygroundApp",
    "request": "launch",
    "mainClass": "com.effacy.jui.playground.PlaygroundApp",
    "vmArgs": "-Dspring.profiles.active=dev",
    "projectName": "jui-playground"
}
```
You should be able to run this and if successful the console should show the initiation of a standard Spring Boot application. Open `http://127.0.0.1:8080/playground` to view the playground.

### Launch from Eclipse

*To be provided.*



## Codeserver

The codeserver is needed during development to compile and serve JUI code on-demand (rather than having to perform a full compilation each time a change is made). The simplest approach is to run it via Maven from the root directory of this module:

```bash
mvn -Pcodeserver
```

or from the parent project root:

```bash
mvn -Pcodeserver -f jui-playground/pom.xml
```

The codeserver admin console can be viewed from  `http://127.0.0.1:9876`. If you have not already configured your browser for compilation (see the [Getting started](https://juiproject.github.io/jui-stack/#/intro_gettingstarted) guide in the JUI documentation) simply follow the instructions to install the bookmarklets.

Once your browser is configured go back to the playground `http://127.0.0.1:8080/playground` and activate the *Dev Mode On* bookmark. This will kick off a compilation.

# Development

*The intention is that this playground is for your own personal use and development, so nothing is expected (or should be) checked in (unless you choose to clone it yourself). However, if you are maintaining the project then the same principles apply though ensure that you* **only** *make changes to the* `develop` *branch.*

You should be able to make a change in any of the referenced projects, perform a build on the project then initiative a UI build (by the code server) from the browser by clicking on the `Dev Mode On` bookmark (recall this is setup as per the instructions at `http://127.0.0.1:9876` as described above). Note that a build is required for the changes to be detected by the code-server so this is a case where automatic building can be quite advantageous (though it can be a little annoying, an alternative is to setup a hot-key in the IDE for initiating a project build).

Sometimes the code-sever won't pickup process (or detect) a change. In this case navigating to `http://127.0.0.1:9876` and selecting the `clean` option will force the code-server to clear its cache and perform a full build on the next build request (by clicking on the `Dev Mode On` bookmark). This is usually enough to fix odd problems that arise due to incomplete detection of changes.

