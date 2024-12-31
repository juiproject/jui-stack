# GWT Compilation Service

## Compiler

The GWT compiler can be programmatically invoked by calling `Compiler.compile(TreeLogger, CompilerOptions, MinimalRebuildCache, ModuleDef)`. Here we pass a logger, options to the compiler, a cache the compiler can use to compile incrementally and the module definition for the module being compiled.

### Classpath

The compiler (by default) expects the compilation sources to be available on the classpath, that means that the classpath must contain both the compiler jar (`gwt-dev.jar`) and the JUI sources. When it comes to the codesever then the codeserver classes (and dependencies) must too be included. This project compiles to a jar that includes dependencies (which includes `gwt-dev.jar`) so in practice the classpath must be of the form:

1. Code sever JAR (with all its dependencies bundled).
2. JUI sources (plus all library dependencies that include compilable code).

Normally the latter is embedded in the broader classpath of the project itself so to run the code server one just needs to expand on that classpath by adding up front the coder server JAR. However, the code server makes use of Spring Boot which can conflict with the project itself should it use Spring Boot (the conflict comes with autoconfiguration in that the code server will attempt to autoconfigure from JARs from the project).

An additional problem is that when working with a multi-module project you cannot rely on the classpath to reference concrete source directories from other projects that include compilable code.

The current solution is:

1. Run the code server from a separate project if the web project includes conflicting Spring Boot autoconfiguration (i.e. the `-jui` project if using a separate one for components).
2. Manually add the source directories from other projects to the classpath. Normally quite easy to do when using the IDE's launch confurations.

The `jui-maven-plugin` does something similar in that you must sepecify sources (though if you don't it will assume only those of the project itself are intended) as well as specify jar inclusions and exclusions (there are some fairly obvious standard inclusions, such as the JUI jars and their dependencies, so these are passed by default - other projects in a multi-module context will likely be specified by sources so don't need to be included - so in the most part one can get away just with the default inclusions and no specific exclusions, which is fairly easy to setup).

*The GWT compiler does include in the module definition (`ModuleDef`) the ability to specify a `ResourceLoader`. The default used by the code server is `ResourceLoaders.fromContextClassLoader ()`. A future enhancement may be to pass an alternative classpath for compilation through and use a cusom resource loader that loads from that. That way the codeserver needs only run from a classpath that includes itself and the classpath for the entire project can be passed through to a resource loader. This does not solve the issue of sources in a multi-module project.*

### Arguments

#### Compiler options

These are generated from the passed `Options` (derived from the program arguments) and defaults. The resulting alignment is described in the following table.

|Option|Assignment|Sourced from|
|------|----------|------------|
|moduleNames|Lists.newArrayList(moduleName)|The module being compiled.|
|deployDir|compileDir.getDeployDir()|`{root}/war/WEB-INF/deploy`|
|extraDir|compileDir.getExtraDir()|`{root}/extras`|
|genDir|compileDir.getGenDir()|`{root}/gen`|
|warDir|compileDir.getWarDir()|`{root}/war`|
|workDir|compileDir.getWorkDir()|`{root}/work`|
|shouldGenerateJsInteropExports|options.shouldGenerateJsInteropExports()|`shouldGenerateJsInteropExports`|
|jsInteropExportFilter|options.getJsInteropExportFilter()|This is the `WhitelistRegexFilter`.|
|logLevel|options.getLogLevel()|`logLevel`|
|methodNameDisplayMode|options.getMethodNameDisplayMode()|`methodNameDisplayMode`|
|output|options.getOutput()|`style` (if not specified then `OBFUSCATED` if incremental otherwise `PRETTY`)|
|properties|options.getProperties()|`setProperty`|
|sourceLevel|options.getSourceLevel()|`sourceLevel`|
|incrementalCompileEnabled|options.isIncrementalCompileEnabled()|`incremental`|
|strict|options.isFailOnError()|`failOnError`|
|closureCompilerFormatEnabled|options.isClosureFormattedOutput()|`closureFormattedOutput` (output can be compiled by CL)|
|shouldJDTInlineCompileTimeConstants|!isIncrementalCompileEnabled()||
|namespace|JsNamespaceOption.PACKAGE|Use package names for class namespaces.|
|optimizationLevel|OptionOptimize.OPTIMIZE_LEVEL_DRAFT|Minimal optimization.|
|sourceMapFilePrefix|Constants.SOURCEROOT_TEMPLATE_VARIABLE|Location of sourcemaps via the code server.|

Note that `{root}` comes from `{workDir}/{module}/` and `{workDir}` is passed as an argument or generated in a temporary location.

#### Module definition

This contains information about the module being compiled along with the heirarchy of imported modules that contribute code. The compiler uses this to determine where to look for sources to compile.

This is generated as normal but with addition configuration that is applied explicityly:

|Option|Value|
|------|-----|
|devModeRedirectEnabled|false|
|installCode|false|
|installScriptJs|com/google/gwt/core/ext/linker/impl/installScriptDirect.js|
|computeScriptBaseJs|com/effacy/jui/codeserver/gwt/computeScriptBase.js|
|includeSourceMapUrl|http://" + serverPrefix + Constants.sourceMapLocationTemplate (moduleDef.getName())|
|CssResource.style|stable (only set if options.isIncrementalCompileEnabled())|
|includeBootstrapInPrimaryFragment|false|
|permutationsJs|com/google/gwt/core/ext/linker/impl/permutations.js|
|propertiesJs|com/google/gwt/core/ext/linker/impl/properties.js|
|compiler.useSourceMaps|true|
|compiler.useSymbolMaps|false|
|superdevmode|on|

### Compilation

First a compilation is performed. These get written to?