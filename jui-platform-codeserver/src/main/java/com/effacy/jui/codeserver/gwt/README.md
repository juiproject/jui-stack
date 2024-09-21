# GWT Compilation Service

## Compiler

The GWT compiler can be programmatically invoked by calling `Compiler.compile(TreeLogger, CompilerOptions, MinimalRebuildCache, ModuleDef)`. Here we pass a logger, options to the compiler, a cache the compiler can use to compile incrementally and the module definition for the module being compiled.

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