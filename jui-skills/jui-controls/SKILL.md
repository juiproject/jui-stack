---
name: jui-controls
description: "Create JUI controls — interactive form field components used in GWT/JUI applications. Use this skill when the user asks to: create a new control, build a custom form field, create an interactive input component, or asks about control patterns. Also trigger when the user mentions control, form field, value management, dirty detection, or asks how to create a reusable input that can be placed in a ControlForm."
---

# JUI Controls

Create custom controls that integrate with JUI's value management, validation, dirty detection and form mechanisms.

## Control vs Component

Controls extend `Control<V, C>` (which itself extends `Component`) and add:

- **Value management** — `setValue(V)` / `value()` with dirty detection
- **Validation** — validators, invalidation state, error message handling
- **State** — read-only, waiting, suspended, empty, disabled
- **Form integration** — works with `ControlForm`, `ControlContext`, acceptors

Use a control when the UI element captures or presents a user-editable value. Use a component (see `jui-components` skill) for display-only UI.

## Minimal Template

```java
public class MyControl extends Control<String, MyControl.Config> {

    public static class Config extends Control.Config<String, Config> {

        @Override
        @SuppressWarnings("unchecked")
        public MyControl build(LayoutData... data) {
            return build(new MyControl(this), data);
        }
    }

    private HTMLInputElement inputEl;

    public MyControl(Config config) {
        super(config);
    }

    @Override
    protected String valueFromSource() {
        if (inputEl == null)
            return null;
        return inputEl.value;
    }

    @Override
    protected void valueToSource(String value) {
        if (inputEl != null)
            inputEl.value = (value != null) ? value : "";
    }

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            Input.$(root, "text").by("input")
                .on(e -> modified(), UIEventType.ONKEYUP, UIEventType.ONPASTE);
        }).build(dom -> {
            inputEl = (HTMLInputElement) manageFocusEl(dom.first("input"));
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IControlCSS {
        // Declare custom styles here.
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS
    }, stylesheet = """
        .component {
            /* Control container styles */
        }
    """)
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
}
```

## Required Methods

### `valueFromSource()`

Extract the current value from DOM or internal state. Called by the framework after `modified()` is invoked.

```java
@Override
protected String valueFromSource() {
    if (inputEl == null)
        return null;
    return StringSupport.safe(inputEl.value);
}
```

### `valueToSource(V value)`

Apply a value to the DOM / internal state. Called when `setValue()` is invoked externally.

```java
@Override
protected void valueToSource(String value) {
    if (inputEl != null)
        inputEl.value = StringSupport.safe(value);
}
```

### `buildNode(Element el, Config data)`

Render the control's DOM. Note that controls use the two-argument version `buildNode(Element el, Config data)` where `data` is the configuration. This differs from `SimpleComponent` which uses `buildNode(Element el)`.

```java
@Override
protected INodeProvider buildNode(Element el, Config data) {
    return Wrap.$(el).$(root -> {
        root.style(styles().component());
        // Build control DOM
    }).build(dom -> {
        // Extract elements
    });
}
```

## Signalling Changes

Call `modified()` from event handlers when user interaction changes the control's value:

```java
Input.$(root, "text").by("input")
    .on(e -> modified(), UIEventType.ONKEYUP, UIEventType.ONPASTE);
```

This triggers `valueFromSource()`, updates the dirty state, and fires `IModifiedListener` events.

## Value Lifecycle

```
setValue(V) ──> valueToSource(V) ──> DOM updated
                                         │
                                    user interacts
                                         │
                                    event handler
                                         │
                                    modified()
                                         │
                                    valueFromSource() ──> value() returns new value
                                         │
                                    dirty detection (compare to reset value)
                                         │
                                    IModifiedListener.onModified() fired
```

## Optional Overrides

### `prepareValueForAssignment(V value)`

Normalise values before storage (e.g., map `null` to empty list):

```java
@Override
protected List<String> prepareValueForAssignment(List<String> value) {
    if (value == null)
        return new ArrayList<>();
    return value;
}
```

### `boolean empty(V value)`

Define when the value is considered empty. Default checks for `null`, empty `String`, empty `Collection`.

```java
@Override
protected boolean empty(MyValue value) {
    if (value == null)
        return true;
    return value.items().isEmpty();
}
```

### `V clone(V value)`

Clone the value when the framework needs an independent copy (for reset value, dirty comparison). Default assumes immutability; override for mutable value types.

```java
@Override
protected FormattedText clone(FormattedText value) {
    if (value == null)
        return null;
    return value.clone();
}
```

### `boolean equals(V v1, V v2)`

Custom equality for dirty detection. Default uses object equality.

```java
@Override
protected boolean equals(FormattedText v1, FormattedText v2) {
    if (v1 == v2)
        return true;
    if ((v1 == null) || (v2 == null))
        return false;
    return v1.computeHash() == v2.computeHash();
}
```

## Focus and Blur

Register focusable elements using `manageFocusEl()`:

```java
.build(dom -> {
    inputEl = (HTMLInputElement) manageFocusEl(dom.first("input"));
});
```

The first registered element becomes the default focus target. The `IComponentCSS.focus()` style is automatically toggled.

## CSS Rules for Controls

Controls differ from components in their CSS setup:

- `ILocalCSS` extends `IControlCSS` (not `IComponentCSS`)
- `IControlCSS` extends `IComponentCSS` and adds: `invalid()`, `read_only()`, `waiting()`
- `@CssResource` must include **both** `IComponentCSS.COMPONENT_CSS` and `IControlCSS.CONTROL_CSS`
- `IComponentCSS.COMPONENT_CSS` provides: `.component`, `.disabled`, `.focus`
- `IControlCSS.CONTROL_CSS` provides: `.invalid`, `.read_only`, `.waiting`

```java
@CssResource(value = {
    IComponentCSS.COMPONENT_CSS,
    IControlCSS.CONTROL_CSS
}, stylesheet = """
    .component {
        /* ... */
    }
""")
public static abstract class LocalCSS implements ILocalCSS { ... }
```

## Full Template with Style Variants

```java
public class XXXControl extends Control<T, XXXControl.Config> {

    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    public static class Config extends Control.Config<T, Config> {

        public interface Style {

            public ILocalCSS styles();

            public static Style create(ILocalCSS styles) {
                return () -> styles;
            }

            public static final Style STANDARD = Style.create(StandardLocalCSS.instance());
        }

        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public XXXControl build(LayoutData... data) {
            return build(new XXXControl(this), data);
        }
    }

    public XXXControl(XXXControl.Config config) {
        super(config);
    }

    @Override
    protected T valueFromSource() {
        // Extract value from DOM or internal state.
        return null;
    }

    @Override
    protected void valueToSource(T value) {
        // Apply value to DOM or internal state.
    }

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            // Build control DOM and event handlers.
        }).build(dom -> {
            // Extract element references.
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return config().style.styles();
    }

    public static interface ILocalCSS extends IControlCSS {
        // Custom style methods.
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS
    }, stylesheet = """
        .component {
            /* ... */
        }
    """)
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create(StandardLocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
```

## Creator Class

```java
public class XXXControlCreator {

    public static XXXControl.Config create() {
        return new XXXControl.Config();
    }

    public static XXXControl build(Consumer<XXXControl.Config> cfg, LayoutData... data) {
        XXXControl.Config config = new XXXControl.Config();
        if (cfg != null)
            cfg.accept(config);
        return config.build(data);
    }

    public static XXXControl $(ContainerBuilder<?> el, Consumer<XXXControl.Config> cfg) {
        return With.$(build(cfg), cpt -> el.render(cpt));
    }
}
```

## Composing Controls

Controls can compose other components internally. Use `Cpt.$(parent, child)` to insert child components into the control's DOM:

```java
@Override
protected INodeProvider buildNode(Element el, Config data) {
    editor = new Editor(editorConfig);
    EditorToolbar toolbar = new EditorToolbar(tbConfig);
    editor.bind(toolbar);

    return Wrap.$(el).$(root -> {
        root.style(styles().component());
        Cpt.$(root, toolbar);
        Cpt.$(root, editor);
    }).build();
}
```

## Using Controls in Forms

Controls integrate with `ControlForm` via name and acceptor configuration:

```java
// In a ControlForm constructor:
group(grp -> {
    grp.control("label", "Label text", Controls.text(cfg -> {
        cfg.name("fieldName");
        cfg.acceptor("fieldName");
        cfg.placeholder("Enter value");
        cfg.validator(
            NotEmptyValidator.validator("required"),
            LengthValidator.validator(0, 100, "max {max} characters")
        );
    }), cell -> {
        cell.grow(1).required();
        cell.from(v -> v.getLabel());           // populate from source
        cell.to((ctx, v, cmd) -> cmd.label(v)); // apply to command (dirty only)
    });
});
```

## Modification Handler

Listen for value changes via configuration:

```java
new MyControl.Config()
    .modifiedHandler((ctl, val, prior) -> {
        Logger.info("Changed from " + prior + " to " + val);
    })
    .build();
```

For delayed handling (e.g., search-as-you-type):

```java
.modifiedHandler(DelayedModifiedHandler.create(300, (ctl, val, prior) -> {
    performSearch(val);
}));
```

## Workflow

When creating a new control:

1. **Identify the value type** — what `V` the control manages (String, List, custom type, etc.)
2. **Extend `Control<V, Config>`** — create `Config extends Control.Config<V, Config>` with `build()` method
3. **Implement `valueFromSource()`** — extract value from DOM state
4. **Implement `valueToSource(V)`** — apply value to DOM state
5. **Implement `buildNode(Element, Config)`** — render control DOM with event handlers
6. **Call `modified()`** — from event handlers when user changes the value
7. **Register focus elements** — call `manageFocusEl()` in `.build()` callback
8. **Add CSS** — `ILocalCSS extends IControlCSS`, include both `IComponentCSS.COMPONENT_CSS` and `IControlCSS.CONTROL_CSS`
9. **(Optional) Override `clone()`, `equals()`, `empty()`** — for non-trivial value types
10. **(Optional) Create creator class** — `XXXControlCreator` with `build()` and `$()` methods
