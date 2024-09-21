# Compilation

?> JUI currently makes use of the [GWT](www.gwtproject.org) compiler. There is a body of work to abstract the compilation process of JUI code so that other compilers can be utilised (such as J2CL). Once this process is complete references to GWT will be depreciated and replaced accordingly. However, backward compatibility will be maintained. For those who are interested in the (quite tecnical) details of the GWT compilation process one is referred to an in depth [presentation](https://docs.google.com/presentation/d/1n0BSQGCBkxfHLzDVFCMyWjqTYuraUr09uc7n5n6JuLU/) from Google.

Here we provide a description of how JUI code is compiled to JavaScript and describe the mechanism by which this is achieved. The documentation in broken into three sections:

1. [Overview](#overview) provides a theoretical description of the compilation process and the various steps that are involved as well as the outputs that are generated.
2. [Integration](#integration) describes how JUI compilation is integrated into the build process.
3. [Development](#development) describes how we employ compilation during the development process.

## Overview

Compilation of JUI code involves both the conversion to JavaScript and the packaging of that JavaScript (along with supporting artefacts) into web resources that can be deployed and referenced directly from the browser. The phases involved are:

1. **Scoping** involves the determination of what source code is eligible for compilation.
2. **Rebinding** the process by which JUI compilable code is generated automatically through processor (or generator) classes.
3. **Transpilation** the conversion of JUI compilable Java to JavaScript.
4. **Linking** the creation of web deployable artefacts that allow for inclusion of the code generated in (2) to be brought into an application.

These are described in more details below.

### Scoping

Compilation is performed per module (generally an application will have only one module) which defines a module hierarchy by way it inheriting from other modules (by using `inherit` in the module definition file). Each module itself declares the source code available for compilation (by using `source` in the module definition file) so collectively defines a body of code that is in scope for compilation (noting here that library dependencies are assumed to also include the original source code).

*Note that compilation is naturally scoped by way of entry points (since the entry points are where code is invoked, only code traceable from entry point is in scope for compilation) however there are other considerations such a rebinding where access to the entire scope may be require ahead of compilation.*

### Rebinding

*In general you will not need to employ rebinding directly. However JUI does make use of it in some specific cases (such as internal style generation) so the presentation here seeks to provide a broad understanding of the mechanism while also serving as a springboard for further study.*

The principle behind rebinding this is that some JUI code may be wholly dependent on other code in a manner that it could be created automatically. By shifting to an automated process one reduces complexity (thereby increasing code maintainability and reliability).

We provide a description of rebinding vis-a-vis an example, one that is encountered fairly frequently. 

JUI provides a mechanism for localising styles, a comprehensive description being found in [Styles](ess_styles.md#localised-css). To access these localised styles one makes use of code similar to the following (as taken from `com.effacy.jui.ui.client.button.Button`):

```java
@CssResource({
    IComponentCSS.COMPONENT_CSS,
    "com/effacy/jui/ui/client/button/Button.css",
    "com/effacy/jui/ui/client/button/Button_Override.css"
})
public static abstract class NormalCSS implements ILocalCSS {

    private static NormalCSS STYLES;

    public static ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (NormalCSS) GWT.create (NormalCSS.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
    }
}
```

Note specifically `GWT.create (NormalCSS.class)`. During the rebinding phase the compiler detects usage of `GWT.create (....)` and attempts to locate a *generator* for the specified class. In this case it is found in the `Platform` module as declared in `Platform.gwt.xml`:

```xml
...
<generate-with class="com.effacy.jui.platform.css.rebind.CssDeclarationGenerator">
    <when-type-assignable class="com.effacy.jui.platform.css.client.CssDeclaration" />
</generate-with>
...
```

Tracing the heritage of `NormalCSS` we find it extends `CssDeclaration`, thus the compiler employs `CssDeclarationGenerator` as the generator (note that this class sits outside the scope of JUI compilable code where convention suggests it reside under a sub-package named `rebind`). The compiler will create an instance of `CssDeclarationGenerator` invoking it with the class (`NormalCSS`) to be generated. The expectation is that the generator will build the source code for a new class extending this class so that (with some additional plumbing) `GWT.create(NormalCSS.class)` will return an instance of this new class (if you look at the implementation of `CssDeclarationGenerator` you will see that it extracts the resources declared by the `@CssResource`, parsing them and performing a suitable merge when more than one resource is declared, then injects these into the generated Java file, suitably named from the original by adding `_Impl` to the end; the ultimate effect being that the CSS is inlined).

During rebinding all such occurances of `GWT.create(...)` are processed in this manner with the JUI code based being suitably augmented with generated Java files ready for transpilation.

?>If you find youself in a position where rebinding is advantageous you can uses the aforementioned CSS processing as a guide (another is the rebinding of observables). In lieu of the rebinding mechanism one may also consider [Java Annotation Processing](https://www.baeldung.com/java-annotation-processing-builder) which is a more contemporary approach to automated code generation. However rebinding has some advantages regaring configuration and scope (i.e. via the module hierarchy).

### Transpilation

Transpilation is the process by which Java is converted to JavaScript. The generalised process consists of:

1. Parsing the in-scope Java to an internal representation (a Java Abstract Syntax Tree, or Java AST).
2. Performing a series of optimisations and normalisations (for example, constant folding and type replacement).
3. Tranforming the representation to one that is JavaScript oriented (conversion to a JavaScript AST).
4. Performing additional optimoisations and normalisations.
5. Generation of the final JavaScript (including demarcations for code splitting).

There is one more consideration and that is that Java tends to have a reliance on JRE core libraries. These do not always translate well to JavaScript and the compiler will often augment these with alternatives that are JavaScript compatible (though may be reduced in functionality and not fully representative, for example `java.lang.Thread` is not supported).

The output from compilation is feed into the linking phase.

### Linking

Linking takes the results of compilation and generates the final artefacts for deployment. These generally consist of:

1. Resources contained in the `public` directories off the module package for each referenced module.
2. Bootstrap code (the `.nocache.js` file) that determines which JS file to load (based on a combination of browser and locale where variants of these have been declared for compilation).
3. The JavaScript files (the `.cache.js` files and those under `deferred/<n>.js` arising from code splitting).
4. Supporting assets such as sourcemaps.

## Integration

Here we describe how compilation is integrated into the build process.

## Development

During development continually performing projects builds is not a viable approach (this tends to be a broadly scoped and lengthy process). To provide a bridge JUI offers a development tool called the *code server*.

The code server runs alongside your application (which will often be running from your IDE) that performs incremental builds on demand. The incremental nature means that only those areas of the code impacted by your change are recompiled, which dramatically reduces the time it takes from code to test.

A full description of how this is setup and used is provided in the [code server documentation](app_codeserver.md).