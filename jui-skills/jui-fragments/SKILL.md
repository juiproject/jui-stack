---
name: jui-fragments
description: "Create JUI fragments — reusable DOM building blocks used in GWT/JUI applications. Use this skill when the user asks to: create a new fragment, build a reusable UI component that is not a full Component, create a DOM helper, or asks about fragment patterns. Also trigger when the user mentions fragment, DOM builder, or asks how to create a reusable piece of UI that can be inserted into builders."
---

# JUI Fragments

Create fragments — reusable DOM building blocks that contribute to a parent component's DOM tree without being full components themselves.

## Fragment vs Component

| Aspect | Fragment | Component |
|--------|----------|-----------|
| **DOM ownership** | Contributes to parent's DOM | Owns its own root element |
| **Event handling** | Events handled by parent component | Has its own event dispatch |
| **Lifecycle** | No independent lifecycle | Full lifecycle (render, dispose, etc.) |
| **Reusability** | Insertable into any DomBuilder tree | Standalone or child of another component |
| **Use case** | Reusable DOM patterns, visual elements | Interactive UI with state and behaviour |

Use a fragment when you need a reusable DOM pattern that doesn't require independent event handling or lifecycle management. Use a component when you need encapsulated behaviour.

**Key rule**: Fragments only *contribute* to the build structure built by the calling renderer. Never directly invoke `build()` on any node builder within a fragment; use `use(n -> {...})` if you need to access the built DOM node.

## Fragment Types

### `Fragment<F>` — No children

The base fragment type for elements that don't contain other insertable children.

### `FragmentWithChildren<F>` — With children

Extends the base to accept child insertables, rendered into a designated container element.

## Minimal Fragment (no children)

### Constructor renderer

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag() {
        super(parent -> {
            Div.$(parent).style("fragMyFrag").$(inner -> {
                // DOM content
            });
        });
    }
}
```

### Using `builder()` (access to instance methods)

When you need access to instance fields or methods:

```java
public class MyFrag extends Fragment<MyFrag> {

    private String title;

    public static MyFrag $(IDomInsertableContainer<?> parent, String title) {
        MyFrag frg = new MyFrag(title);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag(String title) {
        this.title = title;
        builder(parent -> {
            Div.$(parent).style("fragMyFrag").$(inner -> {
                H3.$(inner).text(title);
            });
        });
    }
}
```

### Build method override

For more control, override `buildInto()`:

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    @Override
    public void buildInto(ElementBuilder parent) {
        Span.$(parent).text("Hello");
        Span.$(parent).text("World");
    }
}
```

The default root element is a `<div>`. Override `createRoot()` to change it:

```java
@Override
protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
    return P.$(parent);
}
```

## Fragment with Children

```java
public class MyFrag extends FragmentWithChildren<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag() {
        super((parent, children) -> {
            Div.$(parent).style("fragMyFrag").$(inner -> {
                H3.$(inner).text("Header");
                // Children are rendered by the framework
            });
        });
    }
}
```

### Build method with children

```java
public class MyFrag extends FragmentWithChildren<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    @Override
    public void buildInto(ElementBuilder parent) {
        Div.$(parent).$(childContainer -> {
            H3.$(childContainer).text("Header");
            // Include children in the container
            super.buildInto(childContainer);
        });
    }
}
```

Or override `build()` directly for full control (requires manual child handling):

```java
@Override
public void build(ContainerBuilder<?> parent) {
    Div.$(parent).$(childContainer -> {
        H3.$(childContainer).text("Header");
        children.forEach(child -> {
            child.build(childContainer);
        });
    });
}
```

## Configuration Methods

Fragments support builder-pattern configuration:

```java
public class MyFrag extends Fragment<MyFrag> {

    private String title;
    private String icon;

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag title(String title) {
        this.title = title;
        return this;
    }

    public MyFrag icon(String icon) {
        this.icon = icon;
        return this;
    }

    public MyFrag() {
        builder(parent -> {
            Div.$(parent).style("fragMyFrag").$(inner -> {
                if (icon != null)
                    Em.$(inner).style(icon);
                if (title != null)
                    Span.$(inner).text(title);
            });
        });
    }
}
```

Usage:

```java
MyFrag.$(parent).title("Hello").icon(FontAwesome.star());
```

## Adornments

Apply adornments (from `css()` calls on the fragment) to the root element:

```java
public MyFrag() {
    super(parent -> {
        Div.$(parent).self(n -> adornments().adorn(n)).$(inner -> {
            // DOM content
        });
    });
}
```

Or at the top level using `Stack`:

```java
public MyFrag() {
    super(parent -> {
        Stack.$(parent).adorn(adornments()).$(inner -> {
            // DOM content
        });
    });
}
```

## Styling

For comprehensive styling guidance see the `jui-styles` skill. Fragments support two approaches:

### Injected CSS (recommended for application fragments)

Create a CSS file in the module's `public` directory and inject it. Scope styles using a unique class name prefix (e.g. `fragMyFrag`):

```java
static {
    CSSInjector.injectFromModuleBase("MyFrag.css");
}
```

```java
Div.$(parent).style("fragMyFrag").$(inner -> {
    Span.$(inner).style("title").text(title);
});
```

### Localised CSS (for library fragments)

Use the localised CSS pattern with `ILocalCSS extends CssDeclaration` when stronger isolation is needed. See the `jui-styles` skill for the full pattern.

## Variations

### Enum-based

```java
public class MyFrag extends Fragment<MyFrag> {

    public enum Variant {
        NORMAL, COMPACT, OUTLINED;
    }

    public static MyFrag $(IDomInsertableContainer<?> parent, Variant variant) {
        MyFrag frg = new MyFrag(variant);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag(Variant variant) {
        super(parent -> {
            Div.$(parent).style("fragMyFrag", variant.name().toLowerCase()).$(inner -> {
                // DOM content
            });
        });
    }
}
```

```css
.fragMyFrag.normal { /* ... */ }
.fragMyFrag.compact { /* ... */ }
.fragMyFrag.outlined { /* ... */ }
```

### Interface-based (extensible by consumers)

For library fragments where consumers need custom variants:

```java
public class MyFrag extends Fragment<MyFrag> {

    public interface Variant {

        public String style();

        public static Variant create(String style) {
            return () -> style;
        }

        public static final Variant NORMAL = create("normal");
        public static final Variant COMPACT = create("compact");
    }

    public static MyFrag $(IDomInsertableContainer<?> parent, Variant variant) {
        MyFrag frg = new MyFrag(variant);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    public MyFrag(Variant variant) {
        super(parent -> {
            Div.$(parent).style("fragMyFrag", variant.style()).$(inner -> {
                // DOM content
            });
        });
    }
}
```

Consumers create custom variants:

```java
public static final Variant MY_CUSTOM = Variant.create("mycustom");
```

## Accessing DOM Nodes

Since fragments don't own their DOM, use `use()` to access built nodes:

```java
public MyFrag() {
    super(parent -> {
        Div.$(parent).use(n -> {
            // n is the built Element — store reference if needed
            rootEl = (Element) n;
        }).$(inner -> {
            // DOM content
        });
    });
}
```

Or use `apply()` for side effects during build:

```java
Div.$(parent).apply(n -> items.put(key, (Element) n));
```

## Usage in Components

Insert fragments into component DOM trees:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        root.style(styles().component());
        MyFrag.$(root).title("Section 1");
        MyFrag.$(root).title("Section 2");
    }).build();
}
```

## Workflow

When creating a new fragment:

1. **Choose base** — `Fragment<F>` for leaf fragments, `FragmentWithChildren<F>` for container fragments
2. **Create static `$()` method** — accepts `IDomInsertableContainer<?>` parent, creates and inserts
3. **Choose rendering** — constructor `super(parent -> {...})`, `builder(parent -> {...})`, or override `buildInto()`
4. **Add configuration** — builder-pattern methods returning `this`
5. **Add styling** — injected CSS with scoping class (preferred) or localised CSS for libraries
6. **(Optional) Add variants** — enum or interface-based for visual variations
7. **(Optional) Handle adornments** — use `adornments().adorn(n)` on root element
