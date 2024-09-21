
# Overview

This project provides a means to explore various JUI concepts and components as well as test changes you make to those (or new) components. In particular this includes:

1. An exemplar application with various examples of components.
2. A tutorial to help you orient yourself to developing components (from visual design to implementation). This is documented in the documentation hub (see next item).
3. A documentation hub that provides a reference to the concepts and specific components.

The playground is intended to be run locally in your IDE (which allows for modification) and is deployed as a Spring Boot application.

**This document describes how to get going with the playground.**

# IDE configuration

It is assumed that you have your IDE setup as per the parent [documentation](../) including having imported the parent project along with each of the module projects (this being one). In the following we will assume all references to *project* are to *this* project in the IDE.

For development and testing purposes we need to:

1. Run up a *code server* that delivers the compiled client in a manner that one can undergo a change-and-test cycle in browser as well as performing runtime debugging (via break points).
2. Run the Spring Boot application that serves as the platform for delivering the client application to the browser.

With respect to (1) the *code server* referenced is a stand-alone (Java) application that plays the role of compiler (see **Java-to-JavaScript: a primer** below) and JavaScript (JS) server (with source maps) accessible on port `9876`. The server also has the ability to detect when changes are made to the JUI source code and re-compile the affected code efficiently.

## Eclipse

### Running the code server

This requires some preparation within Eclipse, however once setup it can be run without further configuration:


1. Install GWT and make available as a library:
    1. Find a suitable location to install the software (i.e. under the Eclipse installation directory) then [download](https://www.gwtproject.org/download.html) and unpack it into that location.
    2. Select **Eclipse** > **Settings...** then choose **Java** > **Build Path** > **User Libraries** and create the library `GWT 2.10.0` selecting the external JARs `gwt-dev.jar` and `gwt-user.jar` from the GWT installation above.
2. Open **Run** > **Run Configurations...** and create a new **Java Application** (name it something sensible like `JUI CodeServer`).
3. Select the project to be `jui-playground` and `com.google.gwt.dev.codeserver.CodeServer` as the *Main class*.
4. Use `-logLevel INFO -port 9876 com.effacy.jui.playground.TestApplication -generateJsInteropExports` for the *Program arguments* (note the specification of the entry point `TestApplication`) and `-Xmx3g -Dorg.eclipse.jetty.LEVEL=INFO -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog` for the *VM arguments*.
5. Under **Dependencies** > **Classpath Entries** make sure to have added:
    1. The GWT library (this can be added with the *Advanced* option selecting *Add library* recalling that you have setup the library as a user library when following the Eclipse setup in the parent [documentation](../)).
    2. The java and resources source code trees for the project (this can be added with the *Advanced* option selecting *Add folders* then selecting these folders from the `jui-playground` project).
    3. The other module projects `jui-core`, `jui-remoting`, etc (these can be added using the *Add projects...* option).
6. Add to favorites (set under the *Display in favorites menu* section in the *Common* tab).
 
The above is very generic and can be mimicked for any IDE (it also appears that it can be run using Maven as well). One of the key observation is that we add the GWT user library but this is not added to this project. The reason for that is that the necessary dependencies are provided through Maven as far as the project goes and the `gwt-dev` jar (included in the library) is only need to run the code server. As it turn out this library includes code that conflicts with that needed by the latest version of Spring. As such we can keep these dependencies quite separate.

You should now be able to run the code server as a run configuration. If run successfully you should see the console message:

```txt
The code server is ready at http://127.0.0.1:9876
```

Open this location in your browser and follow the instructions (this will create some bookmarks which can be used to initiate builds).

You can now move onto the application itself.

### Running the application

In the first instance ensure that you have performed Maven build on the parent project (i.e. using the **Standard Install** run configuration). This is needed to generate the initial assets needed by the web application (namely the GWT hooks).

Turning now to the application, this should be as simple as finding the class `TestApplication` (in the `src/main` code tree), right clicking and selecting **Run As** > **Java Application**. Once you do this the console should show the initiation of a standard Spring Boot application that is hosted on `http://127.0.0.1:8080`. Open this up and you should be taken to the documentation hub. At the top right you should find a link to the playground. Click on that link and a new browser window should open. A UI compilation should start then the application should appear.

**Note:** It is recommended that you rename that resulting run configuration something sensible (i.e. `JUI Playground App`) and setting it as a favorite (set under the *Display in favorites menu* section in the *Common* tab when editing the run configuration). That will enable the application to be run from the run configurations menu.

## VS Code

### Running the code server

In order to run the code server we need the latest GWT installation. Find a suitable location to install GWT (we only need to reference one of the libraries) then [download](https://www.gwtproject.org/download.html) and unpack it into that location. In the following we will refer to the above installation location as `{gwt-location}`.

Now edit the `launch.json` file in VS Code and add the following:

```json
{
    "type": "java",
    "name": "JUI Code Server",
    "request": "launch",
    "mainClass": "com.google.gwt.dev.codeserver.CodeServer",
    "args": "-logLevel INFO -port 9876 com.effacy.jui.playground.TestApplication -generateJsInteropExports",
    "vmArgs": "-Xmx3g -Dorg.eclipse.jetty.LEVEL=INFO -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog",
    "projectName": "jui-playground",
    "classPaths": [
        "$Auto",
        "{gwt-location}/gwt-dev.jar",
        "${workspaceFolder}/jui-util/src/main/java",
        "${workspaceFolder}/jui-util/src/main/resources",
        "${workspaceFolder}/jui-core/src/main/java",
        "${workspaceFolder}/jui-core/src/main/resources",
        "${workspaceFolder}/jui-ui/src/main/java",
        "${workspaceFolder}/jui-ui/src/main/resources",
        "${workspaceFolder}/jui-remoting/src/main/java",
        "${workspaceFolder}/jui-remoting/src/main/resources",
        "${workspaceFolder}/jui-validation/src/main/java",
        "${workspaceFolder}/jui-validation/src/main/resources",
        "${workspaceFolder}/jui-playground/src/jui/java",
        "${workspaceFolder}/jui-playground/src/jui/resources",
    ]
}
```

You will need to replace `{gwt-location}` with the GWT installation mentioned above (n.b. you can reference your user home with `${userHome}`). This classpath configuration makes available the source files to the code server which are needed to compile against.

Now you can run the configuration and if run successfully you should see the console message:

```txt
The code server is ready at http://127.0.0.1:9876
```

Open this location in your browser and follow the instructions (this will create some bookmarks which can be used to initiate builds).

You can now move onto the application itself.

### Running the application

As with the code server add the following to the `launch.json` file:

```json
{
    "type": "java",
    "name": "JUI App Server",
    "request": "launch",
    "mainClass": "com.effacy.jui.playground.TestApplication",
    "projectName": "jui-playground"
}
```

You should be able to run this and if successful the console should show the initiation of a standard Spring Boot application that is hosted on `http://127.0.0.1:8080`. Open this up and you should be taken to the documentation hub. At the top right you should find a link to the playground. Click on that link and a new browser window should open. A UI compilation should start then the application should appear.

# Development

*The intention is that this playground is for your own personal use and development, so nothing is expected (or should be) checked in (unless you choose to clone it yourself). However, if you are maintaining the project then the same principles apply though ensure that you* **only** *make changes to the* `develop` *branch.*

## The playground

You should be able to make a change in any of the referenced projects, perform a build on the project then initiative a UI build (by the code server) from the browser by clicking on the `Dev Mode On` bookmark (recall this is setup as per the instructions at `http://127.0.0.1:9876` as described above). Note that a build is required for the changes to be detected by the code-server so this is a case where automatic building can be quite advantageous (though it can be a little annoying, an alternative is to setup a hot-key in the IDE for initiating a project build).

Sometimes the code-sever won't pickup process (or detect) a change. In this case navigating to `http://127.0.0.1:9876` and selecting the `clean` option will force the code-server to clear its cache and perform a full build on the next build request (by clicking on the `Dev Mode On` bookmark). This is usually enough to fix odd problems that arise due to incomplete detection of changes.

## The tutorial

The tutorial is described in the documentation hub (under the **Tutorial** section) and guides you through creating a simple dashboard. It is expected that you will implement code and run it within the playground. You should find stub-classes to help you progress.

There is also a separate set of lessions that describe in more focus specific aspects of working the JUI concepts.

## Documentation hub

Documentation is delivered using [Docsify](https://docsify.js.org/#/) with the assets residing under the `src/main/resources/docs` directory. These can be modified directly (you by running a build on the running server you should be able to see that changes by performing a hard browser refresh without having to re-start the server).

Note that UML is generated using [PlantUML](https://plantuml.com/guide) and diagrams using [Mermaid](https://mermaid.js.org/).

# Java-to-JavaScript: a primer

This serves as a brief primer to Java-to-JavaScript compilation, you should refer to the documentation hub (referenced above) for a more full and complete description including guidance on using JUI.

The underlying principle is that one can created UI code (code that runs in the browser) in Java then compile that code to JavaScript. In addition that code can make reference to structures native to the browser by way of *shadow* classes that mimic existing JavaScript constructs (and ultimately are replaced by those in the compiled code).

The important element relevant to this discussion is the mechanism of compilation, which is necessarily different to Java-Bytecode (i.e. the usual).

In order to perform this compilation we need a compiler. The compiler of choice is that provided by the GWT project (though J2CL is also a viable candidate) which affords some very nice features, in particular the creation of web-ready assets (including bootstrapping) and a spot compiler for development (the *code server*).

For build-time compilation (i.e. running a build using Maven) we employ the `gwt-maven-plugin` to perform a build. This is directed to create the compiled assets in a prescribed location and that location is generally part of the web deployment (so that it can be served up).

During development the web assets created above allow for one to redirect the sourcing of the JavaScript (as we all associated source maps) to a local server running on port `9876`; this is the *code server*. The code server is a separate Java application that is run alongside the main application code and will efficient compile changes you make to your JUI code during development. He source maps also provide the ability to debug code directly in the browser.

The above should give you a pretty good idea of the Java-to-JavaScript process and where the code server fits in. As noted in the in first paragraph there is more abundant information available for consumption in the documentation.