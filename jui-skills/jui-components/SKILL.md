---
name: jui-components
description: "Create JUI components — reusable UI building blocks used in GWT/JUI applications. Use this skill when the user asks to: create a new component, build a custom UI element, create a panel or widget, or asks about component patterns. Also trigger when the user mentions component, SimpleComponent, StateComponent, buildNode, renderer, DomBuilder, or asks how to create a reusable UI element."
---

# JUI Components

Create components that follow the standard JUI patterns and conventions.

## Component Hierarchy

```
IComponent (interface)
  └── Component<C extends Config>    -- base class, heavy lifting
        ├── SimpleComponent           -- no config needed
        │     └── StateComponent<V>   -- re-renders on state change
        └── Control<V, C>            -- form fields with value management
```

Choose the base class based on the component's needs:

| Base class | When to use |
|------------|-------------|
| `SimpleComponent` | Most components. No formal configuration needed. |
| `Component<Config>` | Component with formal builder-pattern configuration. |
| `StateComponent<V>` | Component that re-renders automatically on state changes. |
| `Control<V, C>` | Interactive form field with value, dirty detection, validation. For controls, use the `jui-controls` skill instead. |

## Rendering Approaches

### Build method (preferred)

Override `buildNode(Element)` and return an `INodeProvider` built via `DomBuilder`:

```java
public class MyComponent extends SimpleComponent {

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            // Build DOM structure using DomBuilder.
        }).build();
    }
}
```

With element extraction via the `.use(n -> {})` callback:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).style(styles().header()).use(n -> headerEl = (Element) n);
        Div.$(root).style(styles().body()).use(n -> bodyEl = (Element) n);
    }).build();
}
```

Or with element extraction via the `.build(dom -> {...})` callback:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).style(styles().header()).by("header");
        Div.$(root).style(styles().body()).by("body");
    }).build(dom -> {
        headerEl = dom.first("header");
        bodyEl = dom.first("body");
    });
}
```

### Constructor rendering (simple components)

For simple or inline components, supply the renderer in the constructor:

```java
public class MyComponent extends SimpleComponent {

    public MyComponent(String title) {
        renderer(root -> {
            H3.$(root).text(title);
        });
    }
}
```

With extraction:

```java
public MyComponent(String title) {
    renderer(root -> {
        H3.$(root).text(title).use(n -> titleEl = (Element)n);
    });
}
```

Or

```java
public MyComponent(String title) {
    renderer(root -> {
        H3.$(root).text(title).by("title");
    }, dom -> {
        titleEl = dom.first("title");
    });
}
```

## DomBuilder Essentials

### Element creation

Each HTML element has a corresponding class with static `$()` methods:

```java
Div.$(parent)           // <div>
Span.$(parent)          // <span>
H1.$(parent)            // <h1>  (also H2, H3, H4, H5, H6)
P.$(parent)             // <p>
A.$(parent)             // <a>
Button.$(parent)        // <button>
Input.$(parent, "text") // <input type="text">
Label.$(parent)         // <label>
Em.$(parent)            // <em>
```

### Common builder methods

```java
Div.$(parent)
    .style("cssClass1", "cssClass2")  // CSS classes
    .id("uniqueId")                    // HTML id attribute
    .attr("data-key", "value")         // arbitrary attribute
    .text("content")                   // text content
    .by("refName")                     // reference for extraction
    .css("margin-top: 1em;")           // inline style
    .css(CSS.WIDTH, Length.pct(100))    // typed inline style
    .testId("test-ref")                // test ID for automation
    .$(inner -> {                      // child builder lambda
        // Build children
    });
```

### Event handling

```java
// Click handler (most common)
Button.$(parent).text("Click me")
    .onclick(e -> handleClick());

// General event handler with event types
Div.$(parent)
    .on(e -> handleEvent(e), UIEventType.ONCLICK, UIEventType.ONMOUSEDOWN);

// Handler with access to the element node
Button.$(parent).text("Action")
    .on((e, n) -> handleWithNode(e, (Element) n), UIEventType.ONMOUSEDOWN);

// Prevent default / stop propagation
Button.$(parent).text("No focus steal")
    .on(e -> {
        e.stopEvent();
        doSomething();
    }, UIEventType.ONMOUSEDOWN);
```

### Inserting child components

Components implementing `IDomInsertable` (all `SimpleComponent` subclasses) can be inserted:

```java
// Using Cpt helper
Cpt.$(parent, myChildComponent);

// Using insert
parent.insert(myChildComponent);

// Using creator helper
ButtonCreator.$(parent, cfg -> {
    cfg.label("Click me");
    cfg.handler(cb -> { /* ... */ cb.complete(); });
});
```

### Text nodes

```java
Text.$(parent, "Some text content");
Text.nbsp();  // non-breaking space: \u00A0
Text.bull();  // bullet: \u2022
```

### Conditional rendering

Use plain `if` statements (not `.iff()`) for conditional DOM:

```java
Wrap.$(el).$(root -> {
    root.style(styles().component());
    if (showHeader) {
        Div.$(root).style(styles().header()).$(header -> {
            H3.$(header).text(title);
        });
    }
    Div.$(root).style(styles().body()).$(body -> {
        // Always rendered
    });
}).build();
```

### Runtime DOM updates

Update the DOM without full re-render by manipulating extracted elements:

```java
// Direct text update
DomSupport.innerText(titleEl, newTitle);

// Rebuild a section (no event handlers)
Wrap.buildInto(bodyEl, el -> {
    P.$(el).text(newContent);
});

// Rebuild a section (with event handler registration via Component)
buildInto(bodyEl, el -> {
    A.$(el).text("Click").onclick(e -> handleClick());
});
```

### Full re-render

When state changes substantially, re-render the entire component:

```java
public void updateData(Data newData) {
    this.data = newData;
    rerender();
}
```

## Configuration Pattern

For components with formal configuration, extend `Component<Config>`:

```java
public class MyComponent extends Component<MyComponent.Config> {

    public static class Config extends Component.Config {

        private String title;
        private boolean compact;

        public Config title(String title) {
            this.title = title;
            return this;
        }

        public Config compact(boolean compact) {
            this.compact = compact;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MyComponent build(LayoutData... data) {
            return (MyComponent) super.build(new MyComponent(this), data);
        }
    }

    public MyComponent(Config config) {
        super(config);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            if (config().title != null)
                H3.$(root).text(config().title);
        }).build();
    }
}
```

Usage:

```java
MyComponent cpt = new MyComponent.Config()
    .title("Hello")
    .compact(true)
    .build();
```

### Record-based configuration (simple cases)

For components with a small fixed set of parameters:

```java
public class MyComponent extends SimpleComponent {

    public static record Config(String title, String icon) {
        public static Config of(String title) {
            return new Config(title, null);
        }
    }

    public MyComponent(Config config) {
        renderer(root -> {
            H4.$(root).text(config.title());
        });
    }
}
```

Usage: `Cpt.$(parent, new MyComponent(MyComponent.Config.of("Title")));`

## CSS / Styling

For comprehensive styling guidance -- localised CSS, CSS variables, style packs, and creating custom styles -- see the `jui-styles` skill. The key points for components:

- `ILocalCSS` extends `IComponentCSS`
- `@CssResource` must include `IComponentCSS.COMPONENT_CSS`
- The `.component` class is applied automatically to the root element
- Scope child styles under `.component` (e.g. `.component .header`)
- Reference styles via `styles().methodName()` (names are obfuscated)
- For style variants, declare a `Style` interface in `Config` (see `jui-styles` skill for full pattern)

## Child Components

### Injection during rendering

The simplest approach — insert child components into the DOM tree:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).style(styles().toolbar()).$(toolbar -> {
            Cpt.$(toolbar, new MyButton("Save", () -> save()));
        });
        Div.$(root).style(styles().content()).$(content -> {
            Cpt.$(content, childPanel);
        });
    }).build();
}
```

### Attachment points (single component slot)

For a single component slot that can be assigned before or after rendering:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).apply(n -> registerAttachmentPoint("content", (Element) n));
    }).build();
}

public void setContent(IComponent cpt) {
    findAttachmentPoint("content").setComponent(cpt);
}
```

### Region points (multiple components with layout)

For container-like behaviour with layout:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).apply(region("CONTENT", MinimalLayout.config().build()));
    }).build();
}

public void add(IComponent cpt) {
    findRegionPoint("CONTENT").add(cpt);
}
```

## Lifecycle Hooks

| Method | When called |
|--------|-------------|
| `onAfterRender()` | After initial render completes |
| `onBeforeRender()` | Before render starts |
| `onDispose()` | When component is disposed |
| `onResize()` | When component is resized |

```java
@Override
protected void onAfterRender() {
    super.onAfterRender();
    // Post-render setup (e.g., add extra CSS classes to root)
    getRootEl().classList.add(extraStyles().scope());
}

@Override
protected void onDispose() {
    super.onDispose();
    // Clean up external listeners, timers, etc.
}
```

## Focus and Blur

Register focusable elements during rendering:

```java
.build(dom -> {
    Element focusEl = manageFocusEl(dom.first("input"));
});
```

The first registered focus element becomes the default. When the component gains/loses focus, `IComponentCSS.focus()` is automatically toggled.

## Creator Classes

Convention for helper classes that simplify component creation:

```java
public class MyComponentCreator {

    public static MyComponent.Config config() {
        return new MyComponent.Config();
    }

    public static MyComponent build(Consumer<MyComponent.Config> cfg, LayoutData... data) {
        return ComponentCreatorSupport.build(new MyComponent.Config(), cfg, null, data);
    }

    public static MyComponent $(ContainerBuilder<?> el, Consumer<MyComponent.Config> cfg) {
        return ComponentCreatorSupport.$(el, new MyComponent.Config(), cfg, null);
    }
}
```

Usage:

```java
// Via creator
MyComponent cpt = MyComponentCreator.build(cfg -> {
    cfg.title("Hello");
});

// Into DOM builder
MyComponentCreator.$(parent, cfg -> {
    cfg.title("Hello");
});
```

## Debugging

Set flags on `Component` (typically in the application entry point):

| Flag | Effect |
|------|--------|
| `Component.DEBUG_RENDER = true` | Log every render/re-render to console |
| `Component.DEBUG_OUTLINE = true` | Border around each component |
| `Component.DEBUG_NAME = true` | Component name in `component` DOM attribute |

## Modal Dialogs

### Simple dialog

```java
public class MyComponent extends SimpleComponent {

    private static IDialogOpener<Void, Void> DIALOG;

    public static void open() {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Void, MyComponent>dialog(
                new MyComponent(), cfg -> {
                    cfg.style(ModalStyle.UNIFORM)
                        .title("My Dialog")
                        .type(Type.CENTER)
                        .width(Length.px(500));
                }, b -> b.label("cancel"), b -> b.label("Confirm"));
        DIALOG.open(null, null);
    }
}
```

### Processing dialog (with result)

Implement `IProcessable<R>` to return a result through the dialog callback:

```java
public class MyForm extends SimpleComponent implements IProcessable<Long> {

    private static IDialogOpener<Void, Long> DIALOG;

    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Long, MyForm>dialog(
                new MyForm(), cfg -> {
                    cfg.style(ModalStyle.UNIFORM)
                        .title("Create")
                        .type(Type.CENTER)
                        .width(Length.px(500));
                }, b -> b.label("cancel"), b -> b.label("Create"));
        DIALOG.open(null, cb);
    }

    @Override
    public void process(Consumer<Optional<Long>> outcome) {
        // Empty optional = failure (dialog stays open).
        // Non-empty optional = success (dialog closes).
        outcome.accept(Optional.of(resultId));
    }
}
```

### Configurable dialog (with input data)

Implement `IEditable<T>` to receive data when the dialog opens:

```java
public class MyEditor extends SimpleComponent implements IEditable<MyData> {

    private static IDialogOpener<MyData, Void> DIALOG;

    public static void open(MyData data) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<MyData, Void, MyEditor>dialog(
                new MyEditor(), cfg -> {
                    cfg.style(ModalStyle.UNIFORM)
                        .title("Edit")
                        .type(Type.CENTER)
                        .width(Length.px(500));
                }, b -> b.label("cancel"), b -> b.label("Save"));
        DIALOG.open(data, null);
    }

    @Override
    public void edit(MyData data) {
        // Populate from data.
    }
}
```

## Behavioural Interfaces

| Interface | Purpose |
|-----------|---------|
| `IEditable<V>` | Component can be loaded with data via `edit(V)` |
| `IResetable` | Component can be reset to initial state |
| `IDirtable` | Component reports dirty state |
| `IProcessable<R>` | Component can process and return a result |
| `IOpenAware` | Component is notified when opened (e.g. in a dialog) |
| `ICloseAware` | Component is notified when closed |
| `IActivateAware` | Component is notified when activated (e.g. tab selected) |

## Workflow

When creating a new component:

1. **Choose base class** — `SimpleComponent` for most cases, `Component<Config>` if formal configuration is needed, `StateComponent` if state-driven re-rendering is desired.
2. **Choose rendering approach** — `buildNode(Element)` override (preferred) or `renderer(...)` in constructor (simple cases).
3. **Build DOM** — Use `DomBuilder` classes (`Div`, `Span`, `Button`, etc.) with `Wrap.$(el).$(...).build()`.
4. **Add styles** — Create `ILocalCSS` / `LocalCSS` inner classes with `@CssResource`. Include `IComponentCSS.COMPONENT_CSS`.
5. **Extract elements** — Use `.by("ref")` during build and `dom.first("ref")` in the `.build()` callback.
6. **Handle events** — Use `.onclick()`, `.on()` etc. on element builders.
7. **Add child components** — Use `Cpt.$(parent, child)` or `parent.insert(child)`.
8. **Add lifecycle hooks** — Override `onAfterRender()`, `onDispose()` etc. as needed.
9. **(Optional) Create creator class** — `MyComponentCreator` with `build()` and `$()` methods.
10. **(Optional) Add dialog support** — Static `open()` method using `ModalDialogCreator`.
