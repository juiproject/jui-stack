# Package

This package contains classes specifically to test DOM constructs (i.e. from components and fragments) in unit tests. This is early stage and will probably end up being moved to the **jui-test** project (once the relevant dependencies are confirmed).

# Usage

The package provides a lightweight fake DOM together with a `GWTBridge` suitable for unit tests that need `GWT.create(...)` and related static GWT helpers.

The most common entry points are:

- `GWTTestBridge`: installs a test `GWTBridge` and allows explicit registrations for `GWT.create(...)`.
- `AbstractDomTest`: installs the fake DOM node factory and provides fluent DOM assertions.
- `CssStub`: creates dynamic test doubles for CSS declaration interfaces.

## DOM Testing

For DOM-oriented tests, extend `AbstractDomTest` and build the fragment or component into the fake root:

```java
public class BtnTest extends AbstractDomTest {

    @Test
    public void buildsButtonDom() {
        build(Btn.$("Save"))
            .child("button", c -> c.exists());
    }
}
```

`AbstractDomTest` ensures the `GWTTestBridge` is installed before building content.

## Registering `GWT.create(...)`

Where a type requires a specific test implementation, register it explicitly:

```java
GWTTestBridge.register(MyType.class, MyTypeStub::new);
```

Registrations are stored by class literal and are intended for test setup. `GWTTestBridge.clear()` removes any registered suppliers.

## CSS Declarations

CSS declarations are handled in two layers:

1. `CssStub` creates a proxy-backed stub for a CSS declaration interface.
2. `GWTTestBridge` uses that stub as a fallback when `GWT.create(...)` is asked for a type assignable to `CssDeclaration`.

Examples:

```java
ILocalCSS css = CssStub.of(ILocalCSS.class);
```

This returns method names directly for zero-argument string methods, so `css.fragment()` returns `"fragment"`.

```java
ILocalCSS css = CssStub.prefixed(ILocalCSS.class, "btn");
```

This prefixes generated style names, so `css.fragment()` returns `"btn-fragment"`.

Overrides can be supplied for specific methods:

```java
ILocalCSS css = CssStub.prefixed(ILocalCSS.class, "btn", Map.of(
    "fragment", "btn",
    "label", "btn-label"
));
```

The generated stub also provides default behaviour for `ensureInjected()`, `getCssText()` and `getCssDeclarations()`.

### Limitations

`CssStub` uses JDK dynamic proxies. That means it can implement interfaces, but it cannot create subclasses of abstract CSS resource classes.

The recommended pattern for testable CSS is therefore:

- declare the CSS contract as an interface extending `CssDeclaration`
- use that interface as the type consumed by test code where practical
- use `CssStub` directly or via `GWTTestBridge`

Many production classes follow the pattern:

```java
public static interface ILocalCSS extends CssDeclaration {
    String fragment();
}

public static abstract class LocalCSS implements ILocalCSS {
}
```

In that shape, the interface is the useful test contract. The abstract class can describe the production `GWT.create(...)` target, but the test stub is still interface-based.

If a test needs a concrete class instance rather than an interface-based CSS declaration, then an explicit registration or handwritten test subclass is required.

# Appendix

## Resolving CSS declaration limitations 

The main friction point is the standard production pattern:

```java
public static abstract class LocalCSS implements ILocalCSS {

    private static LocalCSS STYLES;

    public static ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (LocalCSS) GWT.create(LocalCSS.class);
            STYLES.ensureInjected();
        }
        return STYLES;
    }
}
```

This is convenient in production because `GWT.create(LocalCSS.class)` targets the generated concrete subtype for the abstract CSS resource class. The problem in tests is that `CssStub` is interface-based. A proxy that implements `ILocalCSS` is not a `LocalCSS`, so it cannot safely satisfy:

```java
STYLES = (LocalCSS) GWT.create(LocalCSS.class);
```

That leaves three broad options:

- add a test-specific override that returns `CssStub.of(ILocalCSS.class)` or `CssStub.prefixed(...)`
- change the cached field and creation path to use `ILocalCSS STYLES`
- generate a real subtype of `LocalCSS` at test time using a bytecode manipulation library such as ByteBuddy.

This extension is left for a later enhancement.
