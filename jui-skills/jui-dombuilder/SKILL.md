---
name: jui-dombuilder
description: "Build and update DOM with JUI's DomBuilder. Use this skill whenever rendering or re-rendering DOM in a JUI component, control or fragment: building element trees with Div/Span/H3/A/Button etc. under Wrap.$(el), extracting elements with .by()/.use(), wiring events with .onclick()/.on(), inserting child components, and — critically — updating part of the DOM at runtime. Triggers include: DomBuilder, Wrap, Wrap.buildInto, buildInto, rerender, re-render, refresh part of the DOM, update the DOM, ElementBuilder, INodeProvider, StateComponent, StateVariable, 'my onclick stopped firing after re-render', 'event handler not wired up', 'how do I redraw a section'."
---

# JUI DomBuilder

JUI builds DOM **in Java**, imperatively, through a fluent builder. There are no HTML templates: a
component supplies its DOM by building an element tree, and updates it either by rebuilding a section
or by re-rendering. This skill is the reference for building with DomBuilder **and** — the part that
trips people up most — for updating DOM correctly at runtime so events stay wired.

It is used by the **jui-components**, **jui-controls** and **jui-fragments** skills (each builds DOM);
read it alongside whichever of those you are working in.

## Building an element tree

Each HTML element has a builder class with static `$()` factory methods. Build under a **root**
element, supplied one of two ways.

**`renderer(...)` (most common).** Call `renderer(...)` from the component's constructor and build
directly into the supplied root — no `Wrap.$`, no `.build()`, the framework wires it up. This is the
usual way to give a component its DOM and is what most components use:

```java
public class MyComponent extends SimpleComponent {
    public MyComponent(String title) {
        renderer(root -> {
            root.style(styles().component());
            Div.$(root).style(styles().header()).$(header -> H3.$(header).text(title));
            Div.$(root).style(styles().body());
        });
    }
}
```

`renderer(...)` takes an optional second lambda to read back captured elements:
`renderer(root -> { … Div.$(root).by("body"); }, dom -> bodyEl = dom.first("body"))`.

**`Wrap.$(el)` in `buildNode` (when you need the override).** Override `buildNode(Element)` (components)
or `buildNode(Element, Config)` (controls), build under `Wrap.$(el)`, and return the built
`INodeProvider` with `.build()`. Use this when the rendering needs access to the config or other
override-time context:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        root.style(styles().component());
        Div.$(root).style(styles().header()).$(header -> H3.$(header).text(title));
        Div.$(root).style(styles().body());
    }).build();
}
```

Both paths register events and adopt child components correctly — and both are what `rerender()`
re-invokes, so anything built here stays wired across re-renders. The element classes and builder
methods below are identical in either.

Common element classes: `Div`, `Span`, `H1`–`H6`, `P`, `A`, `Button`, `Input.$(parent, "text")`,
`Label`, `Em`, `Ul`, `Li`, `Text` (text node). Children are added by nesting inside a `.$(inner -> …)`
lambda.

### Common builder methods

```java
Div.$(parent)
    .style("cssClass1", "cssClass2")  // CSS classes (prefer styles().xxx() — obfuscated names)
    .id("uniqueId")                    // id attribute
    .attr("data-key", "value")         // arbitrary attribute
    .text("content")                   // text content
    .by("refName")                     // reference for later extraction
    .css("margin-top: 1em;")           // inline style (string)
    .css(CSS.WIDTH, Length.pct(100))    // typed inline style
    .testId("test-ref")                // test id for automation
    .$(inner -> { /* children */ });
```

### Text nodes

```java
Text.$(parent, "Some text content");
Text.nbsp();   //  
Text.bull();   // •
```

### Conditional rendering

Use plain `if` (not a `.iff()` builder) for conditional DOM:

```java
Wrap.$(el).$(root -> {
    root.style(styles().component());
    if (showHeader) {
        Div.$(root).style(styles().header()).$(header -> H3.$(header).text(title));
    }
    Div.$(root).style(styles().body());
}).build();
```

## Extracting elements for later update

Capture references during the build so you can update them at runtime. Two equivalent styles:

```java
// .use(...) — direct capture into a field
Div.$(root).style(styles().body()).use(n -> bodyEl = (Element) n);

// .by("name") + a build callback that reads them back
Div.$(root).style(styles().body()).by("body");
// ...
}).build(dom -> {
    bodyEl = dom.first("body");
});
```

## Events

Event handlers attach to element builders:

```java
Button.$(parent).text("Click me").onclick(e -> handleClick());

Div.$(parent).on(e -> handleEvent(e), UIEventType.ONCLICK, UIEventType.ONMOUSEDOWN);

Button.$(parent).text("Action").on((e, n) -> handleWithNode(e, (Element) n), UIEventType.ONMOUSEDOWN);

// Stop propagation / prevent default
Button.$(parent).text("No focus steal").on(e -> {
    e.stopEvent();
    doSomething();
}, UIEventType.ONMOUSEDOWN);
```

These handlers are only live if the built node is registered as an **event handler** with a component.
When you build through `buildNode`/`renderer` the framework does this for you. When you rebuild a
section at runtime it is **your** responsibility to build through a path that registers them — see the
next section.

## Inserting child components and fragments

```java
Cpt.$(parent, myChildComponent);     // insert a component
parent.insert(myChildComponent);     // equivalent
ButtonCreator.$(parent, cfg -> { cfg.label("Save"); cfg.handler(cb -> { save(); cb.complete(); }); });
MyFrag.$(parent).title("Section 1"); // insert a fragment
```

Like events, child components are only adopted into the component's lifecycle if built through a
registering path.

---

## Updating the DOM at runtime — the important part

There are three ways to change what is on screen after the first render. **Pick the lightest one that
correctly wires events.** Choosing wrong is the single most common rendering bug in JUI.

### 1. Targeted mutation (no rebuild)

For a text or attribute change, mutate the extracted element directly — nothing to re-wire:

```java
DomSupport.innerText(titleEl, newTitle);
titleEl.classList.add(styles().active());
```

### 2. Rebuild a section

You captured an element (`bodyEl`) and want to redraw its contents. There are two methods that look
identical but behave very differently:

```java
// Wrap.buildInto(...) — STATIC content only.
// Rebuilds the DOM but does NOT register event handlers or adopt child components.
Wrap.buildInto(bodyEl, el -> {
    P.$(el).text(newContent);          // fine: no events, no components
});

// buildInto(...) on the Component — INTERACTIVE content.
// Registers declared event handlers and adopts inserted components, and disposes
// the components previously rendered there.
buildInto(bodyEl, el -> {
    A.$(el).text("Click").onclick(e -> handleClick());   // onclick is live
    Cpt.$(el, new MyButton(...));                         // child adopted
});
```

> **The trap.** If the section you are rebuilding contains **any** `onclick`/`on(...)` handler or any
> inserted component or fragment-with-behaviour, `Wrap.buildInto(...)` will render it but the handlers
> will be dead and the child components never enter the event/lifecycle system. The symptom is "the
> button appears but clicking does nothing after the update". The fix is to call **`buildInto(...)` on
> the component** (the instance method inherited from `Component` — and `Control` extends `Component`,
> so controls have it too), **not** the static `Wrap.buildInto(...)`.
>
> Rule of thumb: static, presentational DOM → `Wrap.buildInto`. Anything interactive → the component's
> `buildInto`. When unsure, use the component's `buildInto` — it is correct in both cases.

`buildInto` also has overloads with an extractor (to recapture elements) and an explicit replacement
key:

```java
buildInto(bodyEl, el -> { … }, dom -> { rowEls = dom.all("row"); });
```

### 3. Re-render the whole component (preferred for substantial change)

When a change affects most of the component, don't hand-patch the DOM. Store the new rendering inputs
as **state on the component** and call `rerender()`. The framework rebuilds via the normal
`buildNode`/`renderer` path, so **all events and child components are wired correctly** with no
`buildInto` bookkeeping. This is the cleaner semantic and the default choice.

```java
private Data data;

public void updateData(Data newData) {
    this.data = newData;   // change the state the renderer reads
    rerender();            // rebuild from buildNode/renderer
}

@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        root.style(styles().component());
        P.$(root).text(data.title());
        Button.$(root).text("Edit").onclick(e -> edit());   // wired, every render
    }).build();
}
```

If a child component is expensive to recreate on each render, preserve it across re-renders with
`reuse(...)` and insert the reused instance in the renderer:

```java
Button btn = reuse(ButtonCreator.build(cfg -> { cfg.label("Increase"); cfg.handler(() -> bump()); }));
renderer(root -> {
    P.$(root).text("Counter: " + count);
    Cpt.$(root, btn);   // same instance, survives rerender()
});
```

### 4. State-driven re-render (`StateComponent<V>`)

When a view is a pure function of some state, you don't drive the re-render yourself at all: externalise
the state into a `StateVariable` and extend **`StateComponent<V>`**, which re-renders automatically when
the state changes. This is the cleanest option when one (possibly shared) piece of state determines what
is on screen.

`StateComponent` is a **component base class**, so its construction — `StateVariable`, mutation methods,
shared state across views, the loading/error lifecycle and navigation-awareness — is documented in the
**jui-components** skill. From a DOM-update standpoint the point is simply: *mutate the state, the
component re-renders through the normal renderer path* (so events and children stay wired, as with
`rerender()`).

Decision order for "the screen needs to change":

| Situation | Use |
|---|---|
| One text/attribute tweak | targeted mutation (`DomSupport.innerText`, `classList`) |
| Redraw a static sub-section | `Wrap.buildInto(el, …)` |
| Redraw an **interactive** sub-section | the component's `buildInto(el, …)` |
| Most of the component changes | `rerender()` over component state |
| The view is a function of shared/external state | `StateComponent<V>` + `StateVariable` |

## Documentation

- [topic_rendering.md](https://github.com/juiproject/jui-stack/blob/main/docs/topic_rendering.md) — rendering model
- [patterns_rendering.md](https://github.com/juiproject/jui-stack/blob/main/docs/patterns_rendering.md) — worked rendering patterns
- [ess_states.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_states.md) — state variables and `StateComponent`
- [ess_events.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_events.md) — events

## Workflow

1. **Build** the tree under `Wrap.$(el).$(root -> …).build()` (or `renderer(...)`), capturing elements
   with `.by()`/`.use()` only where you will update them.
2. **Wire events** with `.onclick()`/`.on()` and insert children with `Cpt.$`/creators.
3. **To update at runtime**, pick from the decision table above — and never use `Wrap.buildInto` for
   interactive content; use the component's `buildInto` or `rerender()`.
4. **For state-driven views**, model the state as a `StateVariable` and extend `StateComponent<V>`
   (see the **jui-components** skill for building one).
