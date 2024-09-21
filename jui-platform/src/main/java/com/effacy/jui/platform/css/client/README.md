# Overview

This is a simple mechanism for generating a CSS resource from a class that implements `CssDeclaration` where the CSS styles are drawn from one or more files on the classpath as specified using the `CssResource` annotation. The following is a typical example:

```java
public interface IMyStyles extends CssDeclaration {
    String style1();
    String style2();
}

@CssResource({"MyStyles.css"})
public abstract static class MyStyles implements IMyStyles {
    private static MyStyles STYLES;
    public static MyStyles instance() { 
        if (STYLES == null) {
            STYLES = (MyStyles) GWT.create (MyStyles.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
    }
}

// Contained in MyStyles.css
.style1 {
    ...
}
.style2 {
    ...
}
```

This is based on the ideas beging GWT's CSS/GSS processing but nowhere near as ambitious (i.e. of very limited scope but able to support CSS3). It does require rebinding but the intention is to (at some point) employ source-level annotation processing.

# Implementation

## Declarations

Styles must be declared (as no-argument methods that return a `String`) in a class that implements `CssDeclaration` and that is instantiated using the rebinding mechanism (at least until annotation processing support as been implemented). Typically the styles will be declared in an intermediary interface that extends `CssDeclaration` for which the generated class implements (which means one have have multiple variants of the CSS for implementing different styles). The generating class must be annotated with `CssResource` that configures for:

1. The classpath resources that contain the CSS declarations (`value`).
2. The mechanism used to combine more than one resources (`combine` with default `Combine.APPEND`).
3. If one is being strict in the sense that every style declared must have a corresponding style in the composed CSS.

The mechanism for CSS combination only applies when there are more than one CSS resource declared:

1. `Combine.APPEND` effectively appends the CSS declarations so they appear one after the other (latter styles override former ones).
2. `Combine.REPLACE` where any latter selectors completely replace earlier selectors (and selectors must fully match).
3. `Combine.MERGE` where latter selector are merged into prior selectors (the effect is similar to appending but the styles are merged in the declaration).

Note that if strict is being applied then rebinding will fail if there is not an exact match of styles.

## Obfuscation

Custom CSS selectors that have been declared are obfuscated. There are two modes: *compact* and *stable*. The compact mode can only be applied when performing a full compilation (incremental mode will generate clashes) and maps selectors to names of the form `JUInnnnnn` where `nnnnnn` is a 6-digit number padding to the left by zeros.  The stable mode will generate names that are unique and repeatable (it uses the class name and method name to construct these) so is suitable for incremental builds as employed with the code server.

The obfuscation mode is set using the `CssDeclaration.style` property declared in `Platform.gwt.xml` settng the value to the desired mode. When using the code server this mode is overriden (see the `Recompiler` class in the code server code base) to enforce stable names when running in incremental mode.

*Note that the code respects the property `CssResource.style` when assigning stable mode, this is to maintain backward compatibility with the GWT code server should it be used.*

## Substitution

There are two mechanims available for value substitution:

1. Declare a line of the form `@def <REFERENCE> <VALUE>;` where occurrances of `<REFERENCE>` that appear in the values of style declarations are replaced by `<VALUE>` (there are some value contraints imposed, i.e. no spaces for one).
2. Declare a line of the form `@eval <REFERENCE> <FUNCTION_CALL>;` where `<FUNCTION_CALL>` is a fully qualfied static method invocation (including parenthesis) to a no-parameter method that returns a string. As with `@def` the returned value is substituted.

Note that these declarations may occur anywhere within a CSS resource, but are scoped to that resource.

*Implementation of substitution makes use of a dedicated parser, see the [README.md](../rebind/README.md) file in the sister `rebind` package for details.*

## Generation

As noted in the example above rebinding is required and is configured in the `Platform.gwt.xml` file. Rebinding will create an instantiatable version of the class being generated which provides the necessary implementation of the style declaration methods and CSS to be injected. Injection is performed by a call to `CssInjector.inject(String)` which injects all CSS registered during the current browser event loop at the end of processing of the loop (in the finally phase).

For more details on generation see the [README.md](../rebind/README.md) file in the sister `rebind` package.