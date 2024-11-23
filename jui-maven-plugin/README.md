# Overview

This project delivers a Maven plugin artefact that enables the compilation of JUI code, within a web project, during the compile phase of the Maven lifecycle.

Note that the plugin adheres to the versioning sequence as defined by the overall JUI stack of projects and thus is tied closely to the version of JUI being used. In the most part there will be no differences other than (maybe) the specific version of compiler being used. On this latter point there is always the possibility of specifying a specific compiler version (i.e. for GWT usinging `2.10.0` rather than, say, `2.12.1`). However care needs to be take should the shift in version affect language features employed in JUI itself (i.e. `2.10.0` does not support records and multi-line strings).

# Design details

## Compilation goal

This is implemented by `CompileMojo` which proceeds by first collecting various configuration parameters which are assembled into a command-line execution that runs the relevant (only GWT currently) compiler (for transpilation of JUI code to JavaScript coupled with linking into deployable artefacts).

The command is run in the context of a classpath being specified through the environment variable `CLASSPATH`. This is created from the source roots for the module being compiled, dependencies as resolved through the module POM and supplemented by the compiler JAR (and associated dependencies). The latter is applied through dynamic dependency resolutions and resolution of said dependencies against the file system (i.e. in the local Maven repository).