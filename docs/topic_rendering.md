# Rendering

*We delve into the details of generating DOM during the rendering phase of a components lifecycle. Among the topics covered are the insertion and configuration of [DOM nodes](#building-dom-structures) and the use of re-usable [fragments](#re-use-with-fragments), the handling of UI [events](#event-handling), how to [reference nodes](#node-referencing) once a DOM structure has been built and how child [components](#components) can be inserted.*

Rendering is the process by which a component renders out its DOM node structure (including the registration of UI event listeners to that structure). This was covered conceptually in [Components](ess_components.md) (in the context of building components) while here we cover rendering more specifically with a focus on the mechanisms available to you to generate DOM structures.

?> **A note on styles** CSS styles are an important element of DOM construction. We describe styles in detail in [Styles](ess_styles.md) which covers the various mechanisms one can employ to access styles via stylesheets (there are several).  For this topic we assume styles are presented as string constants (i.e. `"my_class"`) as one would expect when crafting HTML.

## Component build-node method

A `Component` has a number of ways of rendering DOM, however we concentrate on the `buildNode(...)` family of methods, in particular:

```java
protected INodeProvider buildNode(Element el) {
    // Build DOM and return INodeProvider
}
```

During the rendering phase the component will create its *root* element (this is what is inserted into the parent components DOM). The component then calls the various `buildNode(...)` methods in turn until a value is returned, at which time the component will take it as rendering has been done.

You may either render directly into the passed root element or you may return a node via the `INodeProvider`. The component will check this node and if it is not the same as the root element it will insert it into the root element. In general one simply operates directly on the root element.

?>The returned value will also be interrogated for other meta-data, such as child components to manage and the ability to handler UI events. This allows one to incorporate into the rendering process some complex operations such as event registration and use of other components. These will be described in more detail in later sections.

When it comes to the actual DOM creation JUI makes use of the [Elemental2](https://github.com/google/elemental2) libraries to represent DOM in Java, however they only offer a relatively low-level mechanism for DOM construction (see `DomGlobal`). There are other third-party libraries that facilitate DOM construction (such as [Elemento](https://github.com/hal/elemento) and [React4j](https://react4j.github.io/docs/overview.html)) and these are certainly open for consideration. JUI, however, offers quite a rich DOM construction model oriented around the `DomBuilder` (we described another approach through [Templates](#templates) later), that aligns with the approaches taken by a number of other common UI frameworks.

We now turn to this model and describe its use in detail.

## DOM builder

*The DOM builder mechanism is quite flexible and offers a range of approaches. However, we will adopt a standardised one in this documentation (and which is used more generally across JUI) which is advantageous from a pedagogical standpoint.*

### The mechanism

The `DomBuilder` makes use of a hierarchy of classes, based on `NodeBuilder`, that implements a *builder pattern* based model for constructing DOM.

In use one constructs a representation of the desired DOM structure (with the relevant configuration and event declarations) using these builders. Once complete one then *builds* the concrete DOM node structure by calling the `build()` method on the root builder. This returns an instance of `NodeContext` (which implements `INodeProvider`) that contains the root DOM node along with any associated meta-data (this is used by the parent component to extract event handlers and any child components for management).

The primary classes involved are:

1. `DomBuilder` which provides a collection of static methods to help construct DOM builders.
2. `NodeBuilder` which is the base class for all builders. This provides the underlying lifecycle methods and means for building the concrete DOM node structure. In general this is never maniulated directly.
2. `ContainerBuilder` which extends `NodeBuilder` and allows for the insertion of child `NodeBuilder`s. It also contains helper methods to insert specific DOM builders (that represents specific DOM element types, such as DIV's, SPAN's, etc).
3. `ElementBuilder` which extends `ContainerBuilder` and represets DOM element nodes (and supports the application of attributes, css styles, etc).
4. `Div`, `P`, etc which are simple helper classes that, like `DomBuilder`, provide static methods to construct DOM builders. These are used primarily to improve the visual layout of the code so that it aligns more closely to the familar layout of HTML.

Note that there are other `NodeBuilder` sub-classes but that are generally not accessed directly (i.e. there is one for text content).

In addition we make use of the interfaces `IDomInsertable` and `IDomInsertableContainer` (that supports the insertion of `IDomInsertable`'s) which allows for better abstraction of the builder structure. For example `NodeBuilder` implements `IDomInsertable` while `ContainerBuilder` implements `IDomInsertableContainer` so that the insertion of DOM builders into a container is actually presented as the insertion of `IDomInsertable`s. This means that we can go beyond just unserting other DOM builders but rather insert anything that implements `IDomInsertable` (which is ultimately translated to the insertion of DOM builders). We use this technique to implement re-usable [fragments](#fragments) of DOM as well as support the insertion of other [components](#components).

The above is a fair bit to digest so we move on to presenting some concrete examples and develop a standardised approach to employing the DOM builder mechanism.

### Building a DOM structure

We consider a simple, but illustrative, scenario consisting of a DOM structure whose head is a DIV element containing an EM element (with a CSS style from the `FontAwesome` icon set) and a SPAN element containing some text. We provide several versions of how this can be constructed that first align closely with the commentary above then progressively employ the various helper classes to lead to our standardised, and more expressive, approach.

We begin with the raw building blocks:

```java
protected INodeProvider buildNode(Element el) {
    ElementBuilder outer = DomBuilder.div ();
    outer.styles ("my_class");

    ElementBuilder icon = DomBuilder.em ();
    icon.style (FontAwesome.star ()));
    outer.insert (icon);

    ElementBuilder content = DomBuilder.span ();
    content.text ("Some content to display");
    outer.insert (content);

    NodeContext node = outer.build ();
    return node;
}
```

**For brevity, subsequent examples we will only describe the method body taking the rest (including the return keyword) as given.**

Here we start by creating the root node `outer` by calling `DomBuilder.div ()` that returns an `ElementBuilder` configured to generate a DIV element. To this we add the CSS style `my_class`. We then create a builder that represents an EM element (`icon`), add to that the font awesome styles the render a star icon then add that to our outer DIV. Finally we create a builder that represents a SPAN element (`content`) and add to that some text. This node is also added to our outer DIV. We finish off by actually building this structure with a call to `build()` on the root outer DIV which returns the desired `NodeContext`.

#### First improvement: the $ method

What should be evident with the sample above is that it is quite hard to visualise the resultant structure. We can employ the `T $(Consumer<T> self)` method on `IDomInsertableContainer` (that `ElementBuilder` implements) to improve the layout somewhat:

```java
NodeContext node = DomBuilder.div ().$ (outer -> {
    outer.styles ("my_class");
    outer.insert (DomBuilder.em ().$ (icon -> {
        icon.style (FontAwesome.star ());
    }));
    outer.insert (DomBuilder.span ().$ (content -> {
        content.text ("Some content to display");
    }));
}).build ();
```

Take some time to look at this closely; for example, consider `DomBuilder.em ().$ (icon -> {...})` that creates an instance of an `ElementBuilder` configured to represent an EM element (as before) which then passes itself to the lambda expression passed to `$` (under the variable named `icon`). This approach allows us to leverage the lambda expression as a means to better structure the code making it more readable.

#### Second improvement: helper methods

We can go a step further and make use of the helper methods on `ContainerElement` (which `ElementBuilder` extends):

```java
NodeContext node = DomBuilder.div ().$ (outer -> {
    outer.styles ("my_class");
    outer.em ().$ (icon -> {
        icon.style (FontAwesome.star ());
    });
    outer.span ().$ (content -> {
        content.text ("Some content to display");
    });
}).build ();
```

Here the `outer.insert (DomBuilder.em ()...)` is replaced by `outer.em ()` that creates, inserts and returns the relevant element builder.

#### Final improvement: helper classes

Finally we can make use of the helper classes `Div`, `Em` and `Span`:

 ```java
NodeContext node = Div.$ ().$ (outer -> {
    outer.styles ("my_class");
    Em.$ (outer).$ (icon -> {
        icon.style (FontAwesome.star ());
    });
    Span.$ (outer).$ (content -> {
        content.text ("Some content to display");
    });
}).build ();
 ```

These helper classes declare two methods `$()` and `$(IDomInsertableContainer)`. The first simply creates an instance of the appropriately configured `ElementBuilder` (as seen by `Div.$ ()`) while the second creates an instance, inserts that into the passed container and returns the instance for further configuration (as seen by `Em.$ (outer)`).

?>Keep in mind that there we have introduces three versions of the `$(...)` method. Two are reside on the helper classes (as just described) and form a pattern of method declaration that will be used repeatedly. The other resides on `IDomInsertableContainer` and takes a lambda expression that acts on self. This is purely a convenience that allows one to employ a lambda expression that aids in presenting a nested (and HTML-like) visual layout.

Combining all the observations above can express the above even more compactly:

 ```java
NodeContext node = Div.$ ().styles ("my_class").$ (outer -> {
    Em.$ (outer).style (FontAwesome.star ());
    Span.$ (outer).text ("Some content to display");
}).build ();
 ```

It should be evident that these last two examples provide a better visualisation of the resulting DOM node structure (akin to the familiar HTML layout) and one can create quite complex arrangements with little confusion:

```java
NodeContext node = Div.$ ().styles ("my_class").$ (outer -> {
    Em.$ (outer).style (FontAwesome.star ()),
    Span.$ (outer).text ("Some content to display"),
    Div.$ (outer).style ("inner_text").$ (div -> {
        P.$ (div).text ("This is a paragraph");
        P.$ (div).text ("This is another paragraph");
        Ul.$ (div).$ (list -> {
            Li.$ (list).text ("Item 1");
            Li.$ (list).text ("Item 2");
        })
    })
}).build ();
```

The above is the standardised approach we take in this documentation and across the JUI examples in the playground. 

?> Note that not all elements are represented by helper classes. Should you find one that is not you can use the custom helper `Custom.$ ("my-tag")` and `Custom.$ (parent, "my-tag")`. Also, the use of `$` as a method is fairly non-standard, however it is very compact. JUI makes use of this explicitly when dealing with DOM builders and adheres to the general pattern employed by the helper classes. This will be discussed in more details when we describe [fragments](#fragments) and the insertion of [components](#components).

#### More on the $ method

We end this section with a further observation that the helper class method `$()` is technically `$(IDomInsertable...)` (which takes an aribtrary number of insertables as argument). The method creates an appropriately configured instance of `ElementBuilder` but before returning it inserts all the passed insertables. This allows us to recast the previous example even more expressively as:

```java
NodeContext node = Div.$ ().styles ("my_class").$ (
    Em.$ ().style (FontAwesome.star ()),
    Span.$ ().text ("Some content to display"),
    Div.$ ().style ("inner_text").$ (
        P.$ ().text ("This is a paragraph"),
        P.$ ().text ("This is another paragraph"),
        Ul.$ ().$ (
            Li.$ ().text ("Item 1"),
            Li.$ ().text ("Item 2")
        )
    )
).build ();
```

Although this presentation can be quite comforting it may suffer from IDE imposed code formatting and it is not as amenable to loop and conditional logic as the previous examples. It has its place, however we refrain from using it in this documentation.

### Wrapping the root node

The examples given so far create a detached DOM element (i.e. `DomBuilder.div (...).build ()` which creates a detached DIV element that is the root element of an underlying DOM structure) which is accessible via the `INodeProvider`. As described in the introduction the component will extract this element and insert it into the root element.

Although this is entirely acceptable more often we insert directly into the root element. The way this is achieved is by *wrapping* it:

```java
protected INodeProvider buildNode(Element el) {
    return DomBuilder.el (el).$ (...).build ();
}

/* or using the helper class Wrap */
protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (...).build ();
}
```

The returned `INodeProvider` actually returns the wrapped node as its root (the component recognises this so does not attempt to insert it). The previous examples can then be written:

```java
Wrap.$ (el).styles ("my_class").$ (outer -> {
    Em.$ (outer).style (FontAwesome.star ());
    Span.$ (outer).text ("Some content to display");
}).build ();
```

**We adopt this as the standard approach.**

Note that you still need to call the `build()` method as that is where the actual DOM node construction occurs; if you don't then nothing gets built into the root element.

### Event handlers

We now turn to the registration of event handlers; namely, how declare and respond to UI events generated from the DOM structure.

This can be achieved by using the `on(...)` family of methods on `NodeBuilder` (the base builder class).  We build on the previous examples to make the SPAN element (that contains the content text) clickable:

```java
Wrap.$ (el).$ (outer -> {
    Em.$ (outer).style (FontAwesome.star ());
    Span.$ (outer).$ (content -> {
        content.text ("Some content to display");
        content.on (e -> {
            // Do something here.
        }, UIEventType.ONCLICK);
    });
}).build ();
```

Here we add an on-click handler to the SPAN element so that when clicked on the specified lambda expression will be invoked. This expression is passed an instance of `UIEvent` which encapsulates a standard browser event exposing associated data (direct and derived) including the target node (we could have specified the expression `(e,n)->{...}` in which case both the `UIEvent` and the SPAN `Node` will be passed).

It is useful to note the following points on event handlers:

1. Any number of handlers can be added to a given element (simply by calling `on(...)` multiple times).
2. Any number of `UIEventType`'s can be specified for a given handler (and `UIEvent` includes the types that generated it).
3. You can specify the relative order of processing of the handlers (this is useful when a handler is being declared on a parent and a child element such that you want the child element handler to be invoked prior to the parent).

There are a number of event handler declarations that are used quite often and shortcut methods are supplied for these, an example being `onclick(...)` specifically for `UIEventType.CLICK`.

As a final note each registered handler is wrapped up in the `NodeContext` returned by the `build()`. This implements `IUIEventHandler` which needs to be registered with component. This happens automatically when using the `buildNode(...)` method:

```java
protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (outer -> {
        ...
    }).build ();
}
```

### Node referencing

It is often useful to extract specific DOM nodes from the generated structure either to operate on them or to stow them for later reference by the component.

To extract nodes we make use of *referencing* using the `by(String)` method. Referenced nodes can then be extracted during build by passing a lambda expression to the `build(...)` method:

```java
Wrap.$ (el).$ (outer -> {
    Em.$ (outer).style (FontAwesome.star ());
    Span.$ (outer).$ (content -> {
        content.text ("Some content to display");
        content.by ("content");
    });
}).build (dom -> {
    Element el = dom.first ("content");
    ...
});
```

Here we call `dom.first(String)` passing the same reference we used when calling `by(String)`. The reason for naming this method `first` is that more than one node can have the same reference, so nodes are gathered by reference as a list. The `first` simply extracts the first element from the list (and generally you will know if you are referencing nodes uniquely). The following is a more extensive example of this point.

```java
List<MyMenuItem> items = ...;
Wrap.$ (el).$ (outer -> {
    Ul.$ (outer).$ (menu -> {
        for (MyMenuItem item : items) {
            Li.$ (menu).$ (menuitem -> {
                menuitem.text (item.name ());
                menuitem.on (e -> item.handleClick (), UIEventType.ONCLICK);
                menuitem.by ("item");
            });
        }
    });
}).build (dom -> {
    dom.all ("item").forEach (item -> {
        // Do something with node.
        ...
    });
});
```

Here we create a simply menu consisting of a DIV containing a UL which contains each menu item as an LI configured by an instance of `MyMenuItem` (assumed to be provided and used to configure the menu items that should be displayed). To each LI is attached a click handler that invokes an external handler specified in its associated `MyMenuItem`. In addition each menu item is referenced by the string `item` which is then referenced (and processed) during build. 

### Adding components

Components implement `IDomInsertable` so can be inserted into a DOM builder using the `insert(...)` method (see the early examples in [building a DOM structure](#building-a-dom-structure)):

```java
Wrap.$ (el).$ (outer -> {
    Div.$ (outer).$ (inner -> {
        inner.insert (/* component instance */); 
    });
}).build ();
```

While the use of `Cpt` to implement the same insertion maintains consistency with the use of helper classes:

```java
Wrap.$ (el).$ (outer -> {
    Div.$ (outer).$ (inner -> {
        Cpt.$ (inner, /* component instance */); 
    });
}).build ();
```

This means that components can be intermingled with other insertables (including other components and DOM builders) in a natural way.

The component is rendered during the call to `build ()` on the DOM builder structure and the component instance is returned in the `NodeContext` (which allows the parent to capture and manage the component as a child).

Akin to the aforementioned DOM helper classes components will generally come with their own helper class called a *creator* (see [Components](ess_components.md) for more on this). The creator class will generally declare a family of `$` methods that assist with configuraing and inserting component instances into DOM builders. For example the following inserts a button component using its creator class `ButtonCreator`:

```java
Wrap.$ (el).$ (outer -> {
    P.$ (outer).text ("This is a button component in another component:");
    Div.$ (outer).$ (inner -> {
        ButtonCreator.$ (inner, cfg -> {
            cfg.label ("Click me");
            cfg.handler (cb -> {
                // Do something here.
                ...
                cb.complete ();
            });
        });
    });
}).build ();
```

This approach maintains consistency with the use of the standard DOM helper classes and the guidelines for component creator `$` methods reflect this (again, see [Components](ess_components.md) for details).

### Inserting direct HTML

A (currently) rudimentary way of inserting direct HTML makes use of the `Html` helper and multi-line strings:

```java
Wrap.$ (el).$ (outer -> {
    Html.$ (outer, """
        <div class='description'>
            <h3>Service description</h3>
            <p>This particular service affords one the...</p>
        </div>
    """);
}).build ();
```

Here HTML is instered directly as presented (this makes use of the `innerHTML` propert on the elemental `Node`). One needs to take note of the following restrictions:

1. It is expected that the content will have a single root level element (in the above example it is the `DIV`).
2. The content is inserted as inner HTML so is not filtered in anyway (i.e. unsafe content remains unchecked).
3. Take care that class name are literal so would need substitution if using the [local styles](ess_styles.md#local-styles) model of CSS.
4. Avoid javascript or declaring event handlers (if you need to then extract them on build with the `use(...)` method and `JQuery`).

Content substitution is also supported in a limited manner:

```java
Html.$(detail, """
    <div class='selected'>
        <div class='icon'>
            <em class='${icon}' style='transform:rotate(90deg)'></em>
            <span>Please select a report to generate</span>
        </div>
    </div>
""", Map.of ("icon", FontAwesome.arrowTurnDown()));
```

The passed map contains the name-value pairs to substitute with references in the template made by way of `${...}`.

Finally one can insert additional children into the HTML content by declaring a (single) content region with `$$`:

```java
Html.$(detail, """
    <div item='reviewers'>
        <h4>Action report</h4>
        <p>This report declares all the actions that have been taken since creation.</p>
        <div>$$</div>
    </div>
""").$ (
    ButtonCreator.$ (cfg -> {
        ...
    })
);
```

In this case the parent element containing the `$$` is considered as the containment node for the passed children (the return type for `Html.$(...)` is `HtmlBuilder` which is an `IDomInsertableContainer` so naturally slots into the builder structure).

Despite the limitations this is a very good approach to building out descriptive content (help, guidance, etc). Overtime it is expected that this mechanism will be enhanced to support a richer templative mechanism for content rendering.

### Re-use with fragments

Fragments provide a way to capture DOM structures that are used repeatedly in an application but do not warrant building a component for them (components add additional overhead and inclusion in the UI event lifecycyle while fragments just contribute DOM and event handlers to an existing DOM construction).

By way of illustration JUI provides a number of standard fragments (see [JUI standard fragments](#jui-standard-fragments)) under the package `com.effacy.jui.ui.client.fragments` and are used in a manner similar to the DOM helper classes:

```java
Wrap.$ (el).$ (root -> {
    Stack.$ (root).horizontal ().gap (Length.em (1)).$ (stack -> {
        Icon.$ (stack, FontAwesome.user ());
        Icon.$ (stack, FontAwesome.user ()).onclick (() -> DomGlobal.window.alert ("Clicked!"));
        Icon.$ (stack, FontAwesome.user ());
    })
}).build ();
```
Here we wrap an existing DOM element (`el`) within which we add an `Stack` fragment (which is nothing more than a DIV element endowed with a flex display). It is configured to stack the contents horizonatlly (in a row) with a spacing of 1em. Within the stack are three `Icon` fragments, each with the same icon displayed and the middle one with an onclick handler (that displays an alert dialog).

Fragments implement `IDomInsertable` so are able to be used exactly in the same way that `ElementBuilders` are (in fact they can be used alongside them). In addition, fragments that contain children implement `IDomInsertableContainer` so are able to have any `IDomInsertable` added to them. In this way they can be used in a manner that is indestinguishable with DOM builders.

We now describe how to create fragments.

#### Adorning fragments

Fragments will generally carry their own styles however one may want to override these (at the root level). This can be achieved with `IFragmentAdornment`'s that are applied using the `adorn(...)` method (there are conveniences for `css(...)` and `style(...)` which are conveyed to the root elememt builder of the fragment).

You may use `FragmentAdornments` as a helper class for standard adornments, however `IFragmentAdornment` is a functional interface so it can be just as easy to employ lambda-expressions.

There are some cases where a fragment may have trouble apply adornments, this is covered in [Creating fragments](#creating-fragments) below.

The following example applies a margin to an `Icon`:

```java
Icon.$ (root).adorn (FragmentAdornments.margin (Insets.em (0.5)));
```

or equivalently:

```java
Icon.$ (root).css ("margin: 0.5em;");
```


#### Creating fragments

Fragments extend `Fragment` (and if children are being added to the fragment then it should extend `FragmentWithChildren`).

In most cases you override the method `buildInto(ElementBuilder)` where the passed element is a DIV that the fragment creates for you to modify (much in the same way that a component creates a root element that is inserted into the parent and provided for you to modify). Note that if you have extended `FragmentWithChildren` then the default implementation of `buildInto(ElementBuilder)` will add in any children that have been registered with the fragment (this means that you must call the super method in your own code to ensure that children are added, passing the DOM node you want the children added to).

As an aside fragments, when added into the DOM builder structure, are actually evaluted just prior to the structure being built. This enables the fragment to be added and then configured. This is also the basis that allows us to model helper classes along the same lines as other helper classes.

To this end the convention is to structure a fragment along the following lines:

```java
public class Thing {

    public static ThingFragment $(/* configuration data */) {
        return new ThingFragment (/* configuration data */);
    }

    public static ThingFragment $(IDomInsertableContainer<?> parent, /* configuration data */) {
        ThingFragment frg = $ (/* configuration data */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class ThingFragment extends Fragment<ThingFragment> {

        /* configuration setters and members */

        @Override
        protected void buildInto(ElementBuilder root) {
            ...
        }

    }
    
}
```

This way one can create and insert fragments with `Thing.$ ()` and `Thing.$ (parent)` and they will behave the same as builders (including the use of `$(...)` when adding children).

?>There is no requirement to structure fragments this was but it is the convention that has been adopted for this documentation.

We make a final note on the use of styles. Styles are employed in fragments in eactly the same way they are employed for components, so we refer you to [Styles](ess_styles.md) for details.

#### Alternative rendering

If one looks at the `Fragment` class we see that when the fragment is added to a DOM structure its `build(ContainerBuilder<?>)` is called. The performs the following:

1. Checks for the existence of a constructor supplied builder and if present delegates to that.
2. If no build is present it calls the `createRoot(ContainerBuilder<?>)` method that creates the root DIV element and adds it to the parent.
3. If the element returned by (2) is non-`null` then it passed that element to the `buildInto(ElementBuilder)` method (that you will typically override).
4. If any adornments have been registered then apply those (see [Adronments](#adornments)).

There are some important observations one can make:

1. You can override the `build(ContainerBuilder<?>)` directly which gives you the most control over the rendering of the fragment. This is the approach you would take if you are adding more than one DOM node into the parent (rare but does arise occasionally).
2. You may also override the `createRoot(ContainerBuilder<?>)` method when you do not want the root node to be DIV. If you choose this path you can sometimes forgoe overriding `buildInto(ElementBuilder)` and simply build out the fragment in `createRoot(ContainerBuilder<?>)`.
3. You can supply a builder during construction. This is described below.

Passing a builder during fragment construction can be done as a lambda-expression which can be quite convenient:

```java
...
public static class ThingFragment extends Fragment<ThingFragment> {

    public ThingFragment(/* configuration data */) {
        super (parent -> {
            ...
        });
    }

}
...
```

or (in fact this is how the `super(...)` version works):

```java
...
public static class ThingFragment extends Fragment<ThingFragment> {

    public ThingFragment(/* configuration data */) {
        builder (parent -> {
            ...
        });
    }

}
...
```

A similar case applies to `FragmentWithChildren` except that you need to pass a `BiConsumer<ContainerBuilder<?>, List<IDomInsertable>>` where the second paramter is a list of the children associated with the fragment.

?>A special note on using this approach is with respect to [adornments](#adorning-fragments). Adornments are only applied using the method described in [Creating fragments](#creating-fragments) (since the method above allows one to add more than one child element to the parent). In this case you need to apply adornments directly. If the target is a fragment itself then you can pass through the adornment by calling `adornments()` (which returns a deferred consolidations of adornments present on the fragment) or by apply directly `adornments()` to the root element builder.

As a final note the form described above was presented as it is safe to use with `FragmentWithChildren` in avoiding name clashing with the `$` method. This is not a problem with `Fragment` so you can abridge this further:

```java
public class Thing extends Fragment<Thing> {

    public static Thing $(IDomInsertableContainer<?> parent, /* configuration data */) {
        Thing frag = new Thing (plan, /* configuration data */);
        if (parent != null)
            parent.insert (frag);
        return frag;
    }

    public Thing(/* configuration data */) {
        super (parent -> {
            ...
        });
    }
}
```

Which provides for a very compact representation.

#### JUI standard fragments

JUI provides a number of fragments under `com.effacy.jui.ui.client.fragments`. This can also serve as a pool of exemplars for creating your own fragments.

Of note is the manner in which these are styled. The approach is the same as the standard JUI components (and controls) so is amenable to re-styling for your own application. More specifically all JUI fragments extend one of `BaseFragment` or `BaseFragmentWithChildren`. Each of these includes a static initialisation of a standard CSS style that make available styles from `FragmentStyles.css` (and the corresponding override `FragmentStyles_Override.css`). Re-styling is as per [Styles](ess_styles.md).  JUI styles actually are non-strict (they are not obfuscated and do not appear in the associated CSS interface) and so adhere to a strict naming style: `juiXXX` where `XXX` is the fragment name (i.e. `juiIcon`).

If you choose to use the JUI standard fragments to learn from, or base your own fragments from, you will observe some additional complexity. This can be illustrated by looking at the `Paper` fragment (abridged):

```java
public class Paper {

    public static PaperFragment $() {
        return new PaperFragment ();
    }

    public static PaperFragment $(IDomInsertableContainer<?> parent) {
        PaperFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class PaperFragment extends APaperFragment<PaperFragment> {}

    public static abstract class APaperFragment<T extends APaperFragment<T>> extends BaseFragmentWithChildren<T> {

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiPaper");
            super.buildInto(root);
        }

        
    }
}
```

As you see the pattern is similar to that described in [Creating fragments](#creating-fragments) with the exception of introducing an intermediary abstract class. In this case the paper fragment is `PaperFragment` but extends the abstract class `APaperFragment` that provides the implementation. Note the parameter type `T` which extends `APaperFragment<T>`. Its use is best described by considering the `Card` fragment that extends `Paper`:

```java
...
    public static class CardFragment extends ACardFragment<CardFragment> {}

    public static class ACardFragment<T extends ACardFragment<T>> extends APaperFragment<T> {

        protected Length width;

        ...

        @SuppressWarnings("unchecked")
        public T width(Length width) {
            this.width = width;
            return (T) this;
        }

        ...

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiCard", ...);
            if (width != null)
                root.css (CSS.WIDTH, width);
            ...
            super.buildInto (root);
        }
    }
...
```

Here the abstract version `ACardFragment` extends `APaperFragment` but note the return type for `width(...)`. This means that `CardFragment` will inhert `width(...)` which will return a `CardFragment` thereby making available all the remaining configuration for the fragment rather than casting down to the base class.

Although more complex this structure enables chaining of configuration and inheritance of fragments. Not all JUI fragments follow this pattern, only those that form a hierarchy of inheritance to.

### Runtime rendering

In many cases you will want to re-render portions of the DOM at runtime (i.e. in response to some user behaviour or on the change of some data).

The first approach is to *re-render* by calling the `rerender()` method on `Component`. This will force the entire component to re-render as if it we being rendered for the first time. This is useful when the component maintains an internal state (see the re-rendering section of [Components](ess_components.md#re-rendering) for details as well as the separate topic [States and state management](topic_themes.md) for a detailed exposition on the special class of *stateful* components). We focus here on the second approach.

The second approach is to clear the context of a DOM node (i.e. obtained by referencing) and build into that node.  The following is a simple illustration:

```java
...
private Element titleEL;

protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (root -> {
        Div.$ (root).by ("title");
    }).build (dom -> {
        titleEl = dom.first ("title");
    });
}

public void updateTitle(String title, String subtitle) {
    Wrap.buildInto (titleEl, n -> {
        H1.$ (n).text (title);
        P.$ (n).text (subtitle);
    });
}
...
```

Here the `Wrap.buildInto (...)` method is employed to clear the existing contents of the element and build a new structure into the element. If you want to extract nodes as well then you can pass an additional extractor:

```java
...
public void updateTitle(String title, String subtitle) {
    Wrap.buildInto (titleEl, n -> {
        H1.$ (n).text (title);
        P.$ (n).text (subtitle);
    }, dom -> {
        // Extract elements here.
    });
}
...
```

However, a key issue with this is any event handlers you declare will not be registered. To this end the `Component` declares concomitant `buildNode(...)` methods that registers the returned object from `Wrap` when that object implements `IUIEventHandler` (this is exactly what happens when the `buildNode(Elememt)` method is called). The above becomes:

```java
...
public void updateTitle(String title, String subtitle) {
    buildInto (titleEl, n -> {
        H1.$ (n).text (title).onclick (e -> {
            // Do something...
        });
        P.$ (n).text (subtitle);
    });
}
...
```

Under-the-hood the returned event handler is registered using `registerEventHandler(IUIEventHandler,String)`. This method takes two arguments, the second of which is a special key that is used to remove any previously registered handler under a key of the same name. This key is calculated so as to be unique for the passed element, thus multuiple invocations against the same element will properly remove any prior event handler being replaced by the new one.

?> The reason we replace the old event handler is that that handler may contain references to DOM nodes that are being removed. If we do not remove the handler then these nodes could avoid garbage collection resulting is a memory leak (in the case there are a lot of such renderings). Now you may not be able to avoid this if you are doing something complicated such as rendering into a tree that is itself re-rendered from time-to-time. In this case target nodes may change and you will loose their unique references. To avoid this problem use the `buildNode(...)` methods that take a custom replacement key (which you can control).

### Operating on nodes

There are several approaches one can take to operate on DOM nodes that are build from a DOM builder. The first is to extract that node during the call to `build(...)` (see [Node referencing](#node-referencing)) then operate on it. The other is to register an *applier* to the node builder using the `apply(Consumer<Node> applier)` method on `NodeBuilder`.

The following illustrates this by attaching to the node a button component (the preferred way for adding single component is actual described [above](#components), however this serves to illustrate how operations of this can work):

```java
Wrap.$ (el).$ (outer -> {
    P.$ (outer).text ("This is a button component in another component:");
    Div.$ (outer).$ (inner -> {
        inner.apply (attach (button = ButtonCreator.build (cfg -> {
            cfg.label ("Click me");
            cfg.handler (cb -> {
                // Do something here.
                ...
                cb.complete ();
            });
        })));
    });
}).build ();
```

Here we create an anonymous *attachment point* (see [Components](ess_components.md) for a description of attachment points and regions) that is associated with the given node. To that attachment point we attache the button component.

### Custom renderers

*Although renderers form a fundamental building block of JUI (particularly JUI components) they are often not employed directly in your code. To create re-usable snippets of DOM you should make use of [fragments](#fragments). This section is therefore included only for completeness.*

Renderers (see [Renderers](#renderers) for details) provide a means of rendering DOM. Typically these are used as an adjunct to the node builder method on `Component` (i.e. one can pass to a component a renderer and it will use that rather that the node builder method; infact, under-the-hood, the `Component` creates a renderer that calls node builder).

An example of a simple renderer is aking to a fragment:

```java
public class ButtonSnippet implements IRenderer {

    @Override
    public IUIEventHandler render(Element el) {
        return Wrap.$ (el).$ (root -> {
            Button.$ (root).$ (btn -> {
                btn.text ("Template button");
            });
        }).build();
    }
    
}

```

Noting that the `build()` method returns an instance of `NodeContext` that implements `IUIEventHandler`. This can be included in the DOM building process using the `render(...)` method:

```java
Div.$ ().$ (div -> {
    div.render (new ButtonSnippet ());
}).build ();
```

In fact you can also declare event handler with this as well:

```java
public class ButtonSnippet implements IRenderer {

    private String title;

    private Invoker onclick;

    public ButtonSnippet(String title, Invoker onclick) {
        this.title = title;
        this.onclick = onclick
    }

    @Override
    public IUIEventHandler render(Element el) {
        return Wrap.$ (el).$ (root -> {
            Button.$ (root).$ (btn -> {
                btn.text (title);
                btn.on (e -> onclick.invoke (), UIEventType.ONCLICK);
            });
        }).build();
    }
    
}
```

with use:

```java
Wrap.$ (el).$ (root -> {
    root.render (new ButtonSnippet (
        "This is a button",
        () -> DomGlobal.window.alert ("The button was pressed")
    ));
}).build ();
```

Again the above is illustrative and one should use fragments for this particular case.

?> Although the above examples make use of an `IRenderer` they will also work with `IDataRenderer` except that you would use the `DomBuilder` method `render(IDataRenderer<D>,D)` instead, passing the relevant data needed for the renderer to render against.