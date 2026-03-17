---
name: jui-styles
description: "Style JUI components, controls, and fragments using localised CSS, CSS variables, and style packs. Use this skill when the user asks to: add or modify CSS for a component/control/fragment, create a custom style variant, theme a component, use CSS variables, or asks about styling patterns. Also trigger when the user mentions ILocalCSS, CssResource, localised CSS, style pack, Config.Style, CSS variables, or asks how to style or theme a JUI element."
---

# JUI Styles

Style components, controls, and fragments using localised CSS with name obfuscation, CSS variables for theming, and style packs for variant support.

## CSS Hierarchy

```
CssDeclaration (base interface)
  └── IComponentCSS         -- component(), disabled(), focus()
        └── IControlCSS     -- invalid(), read_only(), waiting()
```

- **Components** (`SimpleComponent`, `Component<Config>`): `ILocalCSS extends IComponentCSS`
- **Controls** (`Control<V, C>`): `ILocalCSS extends IControlCSS`
- **Fragments**: `ILocalCSS extends CssDeclaration` (or `IComponentCSS` when stronger isolation is needed)

## Localised CSS for Components

The standard pattern for component styling. Styles are obfuscated at compile time to prevent name clashing.

```java
public class MyComponent extends SimpleComponent {

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            Div.$(root).style(styles().header()).$(header -> {
                H3.$(header).text("Title");
            });
            Div.$(root).style(styles().body());
        }).build();
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {

        String header();

        String body();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            display: flex;
            flex-direction: column;
        }
        .component .header {
            padding: 8px 12px;
            font-weight: 600;
        }
        .component .body {
            flex: 1;
            padding: 12px;
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

Key rules:

- `ILocalCSS` extends `IComponentCSS` for components or `IControlCSS` for controls
- `@CssResource` must include `IComponentCSS.COMPONENT_CSS` (and `IControlCSS.CONTROL_CSS` for controls)
- The `.component` class is applied automatically by `SimpleComponent` to the root element
- Scope child styles under `.component` (e.g. `.component .header`) for isolation
- Style method names use underscores not dashes: `my_style()` not `my-style()`
- Always reference styles via `styles().methodName()` (names are obfuscated)

## Localised CSS for Controls

Controls extend the CSS hierarchy with additional state classes.

```java
public static interface ILocalCSS extends IControlCSS {

    String inner();

    String label();
}

@CssResource(value = {
    IComponentCSS.COMPONENT_CSS,
    IControlCSS.CONTROL_CSS
}, stylesheet = """
    .component {
        /* Control container */
    }
    .component .inner {
        /* Input area */
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
```

`IControlCSS` provides the following styles (managed automatically by the framework):

| Style | When applied |
|-------|-------------|
| `component()` | Always on the root element |
| `disabled()` | When the control is disabled |
| `focus()` | When the control has focus |
| `invalid()` | When validation fails |
| `read_only()` | When the control is read-only |
| `waiting()` | When the control is in a loading state |

## Localised CSS for Fragments

Fragments have two styling approaches.

### Injected CSS (preferred for application fragments)

```java
public class MyFrag extends Fragment<MyFrag> {

    static {
        CSSInjector.injectFromModuleBase("MyFrag.css");
    }

    public MyFrag() {
        super(parent -> {
            Div.$(parent).style("fragMyFrag").$(inner -> {
                Span.$(inner).style("title").text("Hello");
            });
        });
    }
}
```

Scope styles using a unique class name prefix (e.g. `fragMyFrag`):

```css
.fragMyFrag {
    display: flex;
    gap: 8px;
}
.fragMyFrag > .title {
    font-weight: 600;
}
```

### Localised CSS (for library fragments)

```java
public class MyFrag extends Fragment<MyFrag> {

    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public interface ILocalCSS extends CssDeclaration {

        String wrapper();

        String title();
    }

    @CssResource(stylesheet = """
        .wrapper {
            display: flex;
            gap: 8px;
        }
        .title {
            font-weight: 600;
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

## CSS Variables

CSS variables enable theming without replacing stylesheets. Declare component-specific variables on the `.component` class and reference them throughout.

### Declaring variables

```java
@CssResource(value = {
    IComponentCSS.COMPONENT_CSS
}, stylesheet = """
    .component {
        --jui-toolbar-bg: #fafafa;
        --jui-toolbar-gap: 2px;
        --jui-toolbar-btn-color: #444;
        --jui-toolbar-btn-hover-bg: #e8e8e8;
        --jui-toolbar-btn-active-bg: #dbeafe;
        --jui-toolbar-btn-active-color: #1d4ed8;
    }
    .toolbar {
        background: var(--jui-toolbar-bg);
        gap: var(--jui-toolbar-gap);
    }
    .tbtn {
        color: var(--jui-toolbar-btn-color);
    }
    .tbtn:hover {
        background: var(--jui-toolbar-btn-hover-bg);
    }
    .tbtnActive {
        background: var(--jui-toolbar-btn-active-bg);
        color: var(--jui-toolbar-btn-active-color);
    }
""")
```

### Overriding variables externally

Users can override variables via an auxiliary CSS class (global or injected):

```css
.my-custom-toolbar {
    --jui-toolbar-bg: #1e293b;
    --jui-toolbar-btn-color: #e2e8f0;
    --jui-toolbar-btn-hover-bg: #334155;
}
```

Applied via `styles(...)` configuration:

```java
cfg.styles("my-custom-toolbar");
```

Or inline via `css(...)`:

```java
cfg.css("--jui-toolbar-bg: #1e293b;");
```

### Naming convention

- Global theme variables: `--jui-color-*`, `--jui-ctl-*`, `--jui-btn-*`, `--jui-state-*`
- Component variables: `--jui-componentname-*` (e.g. `--jui-toolbar-bg`)
- Reference global variables from component variables for theme integration:

```css
.component {
    --cpt-btn-bg: var(--jui-btn-bg);
    --cpt-btn-text: var(--jui-btn-text);
    --cpt-btn-disabled-bg: var(--jui-state-disabled-bg);
}
```

## Style Packs

Style packs allow a component to support multiple visual variants (e.g. a button with normal, outlined, and link styles). Each variant provides its own `ILocalCSS` implementation with different stylesheets.

### Declaring in Config

```java
public class MyComponent extends Component<MyComponent.Config> {

    public static class Config extends Component.Config {

        public interface Style {

            public ILocalCSS styles();

            public static Style create(ILocalCSS styles) {
                return () -> styles;
            }

            public static final Style NORMAL = create(NormalCSS.instance());
            public static final Style COMPACT = create(CompactCSS.instance());
        }

        private Style style = Style.NORMAL;

        public Config style(Style style) {
            if (style != null)
                this.style = style;
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
    protected ILocalCSS styles() {
        return config().style.styles();
    }

    // Style interface shared by all variants.
    public static interface ILocalCSS extends IComponentCSS {

        String header();

        String body();
    }

    // Variant 1: Normal.
    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            border: 1px solid #ddd;
            border-radius: 6px;
        }
        .component .header {
            padding: 12px 16px;
            font-size: 1em;
        }
        .component .body {
            padding: 16px;
        }
    """)
    public static abstract class NormalCSS implements ILocalCSS {

        private static NormalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (NormalCSS) GWT.create(NormalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }

    // Variant 2: Compact.
    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            border: 1px solid #eee;
            border-radius: 4px;
        }
        .component .header {
            padding: 4px 8px;
            font-size: 0.85em;
        }
        .component .body {
            padding: 8px;
        }
    """)
    public static abstract class CompactCSS implements ILocalCSS {

        private static CompactCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (CompactCSS) GWT.create(CompactCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
```

Usage:

```java
new MyComponent.Config()
    .style(MyComponent.Config.Style.COMPACT)
    .build();
```

### Style with additional configuration

The `Style` interface can carry more than just CSS. This is useful when variants differ in structure (icons, layout direction, etc.):

```java
public interface Style {

    public ILocalCSS styles();

    public boolean vertical();

    public String icon();

    public static Style create(ILocalCSS styles, boolean vertical, String icon) {
        return new Style() {
            @Override
            public ILocalCSS styles() { return styles; }
            @Override
            public boolean vertical() { return vertical; }
            @Override
            public String icon() { return icon; }
        };
    }

    public static final Style HORIZONTAL = create(HorizontalCSS.instance(), false, FontAwesome.minus());
    public static final Style VERTICAL = create(VerticalCSS.instance(), true, FontAwesome.plus());
}
```

### Default style override

Components can expose a static `DEFAULT_STYLE` field to allow application-wide defaults:

```java
public static Config.Style DEFAULT_STYLE = Config.Style.NORMAL;

public static class Config extends Component.Config {
    private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.NORMAL;
    // ...
}
```

Applications override the default at startup:

```java
MyComponent.DEFAULT_STYLE = MyComponent.Config.Style.COMPACT;
```

## Creating Custom Styles for Existing Components

Custom styles can be created outside a component's class. This is the primary mechanism for restyling library components.

```java
@CssResource(value = {
    IComponentCSS.COMPONENT_CSS
}, stylesheet = """
    .component {
        border: 2px solid #4f46e5;
        border-radius: 12px;
        background: #eef2ff;
    }
    .component .header {
        padding: 16px 20px;
        color: #4338ca;
        font-weight: 700;
    }
    .component .body {
        padding: 20px;
    }
""")
public abstract class CustomMyComponentCSS implements MyComponent.ILocalCSS {

    public static final MyComponent.Config.Style CUSTOM = MyComponent.Config.Style.create(instance());

    private static MyComponent.ILocalCSS STYLES;

    public static MyComponent.ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (CustomMyComponentCSS) GWT.create(CustomMyComponentCSS.class);
            STYLES.ensureInjected();
        }
        return STYLES;
    }
}
```

Usage:

```java
new MyComponent.Config()
    .style(CustomMyComponentCSS.CUSTOM)
    .build();
```

The custom class implements `MyComponent.ILocalCSS` and provides its own stylesheet. The `Style.create()` factory wraps it into a `Style` instance that can be passed to the component's config.

## File-based Stylesheets

For larger stylesheets, reference external CSS files instead of inlining. Files are resolved relative to the classpath.

```java
@CssResource({
    IComponentCSS.COMPONENT_CSS,
    "com/myapp/ui/MyComponent.css",
    "com/myapp/ui/MyComponent_Override.css"
})
public static abstract class LocalCSS implements ILocalCSS {
    // ...
}
```

The override pattern (`_Override.css`) is used by library components: the main stylesheet contains the defaults and the override file is initially empty. Projects replace the override file using `super-source` to customise without modifying the original.

## Mixing Obfuscated and Plain Styles

Styles declared in `ILocalCSS` are obfuscated and must be referenced via `styles().methodName()`. Styles that exist in the stylesheet but are not declared in `ILocalCSS` retain their original names and can be referenced as plain strings:

```java
// Obfuscated (declared in ILocalCSS)
root.style(styles().header());

// Plain (exists in CSS but not in ILocalCSS)
root.style("my_plain_style");
```

Styles inherited from `IComponentCSS` (such as `component`, `disabled`, `focus`) are always obfuscated and must be accessed via the interface methods.

## Workflow

When styling a JUI element:

1. **Choose the approach** -- localised CSS (recommended for components/controls/library fragments) or injected CSS (for application-level fragments)
2. **Determine the base interface** -- `IComponentCSS` for components, `IControlCSS` for controls, `CssDeclaration` for fragments
3. **Declare `ILocalCSS`** -- add methods for each custom style class
4. **Write the stylesheet** -- inline via `stylesheet = """..."""` or reference external files
5. **Include base CSS** -- `IComponentCSS.COMPONENT_CSS` (and `IControlCSS.CONTROL_CSS` for controls) in the `@CssResource` annotation
6. **Add CSS variables** -- for any values that should be externally configurable
7. **Scope child styles** -- under `.component` (e.g. `.component .header`)
8. **(Optional) Add style packs** -- create a `Style` interface in `Config` with `create()` factory and variant constants
9. **(Optional) Create custom styles** -- implement `ILocalCSS` in an external class with its own `@CssResource` annotation and stylesheet
