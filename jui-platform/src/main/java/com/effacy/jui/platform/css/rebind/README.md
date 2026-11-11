# Overview

This package contains the rebinding code to generate instantiatable classes that implement `CssDeclaration` (see [README.md](../client/README.md) in the sister `client` package for further details).

# Implementation

## Rebind generation

This is performed by `CssDeclarationGenerator` and is enabled by the following in `Platform.gwt.xml`:

```xml
<generate-with class="com.effacy.jui.platform.css.rebind.CssDeclarationGenerator">
    <when-type-assignable class="com.effacy.jui.platform.css.client.CssDeclaration" />
</generate-with>
```

As you can see it is directed to generate those interfaces that extend (in their hierarahcy) `CssDeclaration`. As directives the following is also declared in `Platform.gwt.xml`:

```xml
<define-configuration-property name="CssDeclaration.style" is-multi-valued="false" />
<set-configuration-property name="CssDeclaration.style" value="obf" />
```

The property `CssDeclaration.style` may take the values `obf` (the default) or `stable` and direct how the CSS styles are obfuscated.

The generation process involes:

1. Resolution of `CssDeclaration.style` to determine the obfuscation algorithm to use.
2. Resolution of the CSS files (on the classpath) from the `@CssResource` annotation along with whether style matching in the CSS should be strict and what the CSS style combination mechanism should be employed for multiple CSS files.
3. Each of the CSS files is loaded (in order) into an instance of `CssProcessor` (see [Parsing CSS](#parsing-css)) that performs the parsing of the CSS.
4. The methods are extracted of the type being generated and mapped to candidate style name (that should appear in the parsed CSS). The use of the `@UseStyle` annotation directs collection of styles that should not be obfuscated.
5. The processor instance in (3) is directed to *re-map* the styles to their obfuscated form (taking into consider those styles identified in (4) that should not be obfuscated).
6. Implementations of the style methods on the type being gerenated are created that return the re-mapped (i.e. obfuscated) style names. Here any method that does not have a matching CSS entry in the processor will generate an error and the rebind will fail.
7. Any `@Font` annotations on the type being generated are extracted.
8. The `getCssText()` method (on `CssDeclaration`) is created and returns a stylesheet corresponding to the re-mapped and combined list of CSS files processed in (3) along with ant font declarations arising in (7). Substitutions are also generated at this point (by string replacement).
9. An implementation of `ensureInjected()` is created (the effective component being a call to `CssInjector.inject (getCssText ())`) which injects the generated stylesheet in (8).
10. An implementation of `getCssDeclarations()` is created if directed to by `@CssResource` (see `generateCssDecarations()`).

A note on (8) is that for fonts, the font resources can be quite large. If they exceed a threshold then a copy is transferred to the public deployment area so that it can be accessed as a web resource (rather than injected inline). 

## Parsing CSS

CSS stylesheet parsing is performed by `CssProcessor` making use of the [ph-css](https://github.com/phax/ph-css) CSS parser. The generalised parsing process is as follows:

1. CSS stylesheet are loaded (by `load(...)`) in order.
2. Internally a cache of styles is maintained. As styles are loaded from the respective stylesheets name clashes are processed in accordance with the desired combination rule (as was ultimately sourced from the `@CssResource` annotation).
3. Any `@def` and `@eval` entries (detected as at the start of the line) are processed (see the [README.md](./parser/README.md) in `parser`).
4. Remapping is a process that maps a given style name to a new (obfuscated) on. This is stored internally as a map that can be retrieved by the caller.

Testing of the processor is performed by `CSSTest`.

### Units the parser does not know

ph-css (7.0.1) does not know a number of valid CSS units: `lh`, `rlh`, `cap`, `ic`, `rex`, `rch`, `rcap`, `ric`, `vi`, `vb`, the small, large and dynamic viewport units (`svh`, `lvw`, `dvh`, `svi`, ... `dvmax`), the container query units (`cqw`, `cqh`, `cqi`, `cqb`, `cqmin`, `cqmax`) and the resolution unit `x`. Left to itself it writes `18lh` as `18 lh` (which a browser drops, so the declaration has no effect) and can't read one inside `calc(...)` at all (losing the whole rule).

`CssUnits` takes care of this: before parsing, each such dimension in a declaration value is replaced with a placeholder the parser carries anywhere (`var(--jui-unit-N)`), and the placeholders are put back in `export()` and `declarations()`. So `max-height: 18lh` and `calc(18lh + 2px)` come through as written. Selectors, at-rule preludes (media queries), strings, comments and `url(...)` are left alone, so a unit in a media query (`@media (min-height: 10lh)`) is still written as `10 lh`. The list of units is `CssUnits.UNITS`.

### Reporting what is dropped

Anything the parser can't read (a declaration, a rule or, at worst, the whole stylesheet) is not in its output. Rather than drop it silently, the processor records each problem, available from `CssProcessor#warnings()`, and `CssDeclarationGenerator` logs them as warnings against the type being generated.

## Parsing substitutions

This is described in the [README.md](./parser/README.md) of the `parser` package and is employed by `CssProcessor` to generate a collection of substitutions. These are made available via `CssProcessor#substitutions(BiConsumer<String,String>)` so can be used to generate substiution code. The exported CSS (via `CssProcessor#export()` and `CssProcessor#exportAsString()`) will have been modified so that the substitution references can be safely replaced using string replacement. The details are described in the aforementioned readme.