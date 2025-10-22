# Patterns

Here we provide a collection of design patterns and templates that can be used when developing JUI applications. These may be regarded as a form of *best practice*, though one should take such claims cautiously as they can represent a mode of thought or simplified characterisation that may not be entire fit-for-purpose in your own situation.

This is a reference to some commonly used code that could be useful when developing JUI applications.

1. [Components](#components)
     1. [Construction](#construction)
     2. [Styling](#styling)
     3. [Rendering](#rendering)
     4. [Events](#events)
     5. [Fragments](#fragments)
     6. [Controls](#controls)
2. [Modal dialogs](#modal-dialogs)
     1. [Inline modals](#inline-modals)
     2. [Modal-enabling](#modal-enabling-a-component)
         1. [Simple dialog](#simple-dialog)
         2. [Processing dialog](#processing-dialog)
         3. [Configurable dialog](#configurable-dialog)
         4. [Secondary configuration](#secondary-configuration)
     3. [Modal with resolver](#modal-with-resolver)
     4. [Modal with state](#modal-with-state)
     5. [Modal with open awareness](#modal-with-open-awareness)
     6. [Modal with internal action](#modal-with-internal-action)
3. [Form patterns](#form-patterns)
     1. [Create form](#create-form)
     2. [Update form](#update-form)
     3. [Update extends create](#update-extends-create)
4. [State](#state)
     1. [Value state](#value-state)
     2. [Inlined state](#inlined-state)
5. [JUI components](#jui-components)
6. [Remoting](#remoting)
7. [Stores](#stores)
     1. [Infinite scrolling](#infinite-scrolling)
8. [Examples](#examples)

## Components

### Construction

Starter templates are simple code blocks that fully outline a component and that one can build on.

#### Inlining

The simplest components are those that are inlined and have do dedicated class associated with them:

```java
ComponentCreator.build (root -> {
    // Build DOM structure.
})
```

#### Build method rendering

The following creates a component that uses the **build method**, this is one of the most common approaches:

```java
public class MyComponent extends SimpleComponent {
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // Build DOM structure.
        }).build();
    }
}
```

This builds on the previous recipe by adding **configuration** via the standard configuration mechanism:

```java
public class MyComponent extends Component<MyComponent.Config> {

    public static Config extends Component.Config {

        private String property;

        public Config property(String property) {
            this.property = property;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MyComponent build(LayoutData... data) {
            return (MyComponent) super.build (new MyComponent (this), data);
        }

    }

    public MyComponent(Config config) {
        super (config);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // Build DOM structure.
        }).build();
    }
}
```

#### Constructor rendering

The following creates a component that uses the **rendering in constructor method**:

```java
public class MyComponent extends SimpleComponent {

    public MyComponent(/* config */) {
        renderer (root -> {
            // Build DOM structure.
        });
    }
}
```

The following creates a component that uses the **rendering in constructor method** but with **extraction**:

```java
public class MyComponent extends SimpleComponent {

    public MyComponent(/* config */) {
        renderer (root -> {
            // Build DOM structure.
        }, dom -> {
            // Extraction.
        });
    }
}
```

#### Creator classes

The following is a **creator class** for the previous starter:

```java
public class MyComponentCreator {

    public static MyComponent $(ContainerBuilder<?> el, Consumer<MyComponent.Config> cfg) {
        return $ (el, cfg, null);
    }

    public static MyComponent $(ContainerBuilder<?> el, Consumer<MyComponent.Config> cfg, Consumer<MyComponent> builder) {
        return ComponentCreatorSupport.$ (el, new MyComponent.Config (), cfg, builder);
    }

    public static MyComponent.Config config() {
        return new MyComponent.Config ();
    }
    
    public static MyComponent build(Consumer<MyComponent.Config> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    public static MyComponent build(Consumer<MyComponent.Config> cfg, Consumer<MyComponent> builder, LayoutData...data) {
        return ComponentCreatorSupport.build (new MyComponent.Config (), cfg, builder, data);
    }
}
```
### Styling

#### Adornments

If you are using an existing component that is configuration based then you can use the `adorn(...)` or `css(...)` methods on the base `Config` class:

```java
new MyComponent.Config()
    .css ("background-color: #fff;")
    ...
    .build();
```

or

```java
new MyComponent.Config()
    .adorn (el -> {
        CSS.BACKGROUND_COLOR.apply (el, Color.raw("#fff"));
    })
    ...
    .build();
```

You can also assign CSS variables with `css(...)` (i.e. `css("--my-ccs-variable: #f1f1f1;")`).

#### Localised

The following adds [localised styles](ess_styles.md#localised-css) to a component:

```java
public class MyComponent extends SimpleComponent {

    ...
    
    /********************************************************************
     * CSS
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    public static interface ILocalCSS extends IComponentCSS {
        // Add relevant styles.
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource(value = {  
        IComponentCSS.COMPONENT_CSS,
        ".../MyComponent.css"
    }, stylesheet = """
        .component {
        }
        ...
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
```

Note that `stylesheet` is optional (if used you can avoid using a custom stylesheet resource, or if not used then place your styles in a stylesheet resource).

#### Config styles

The following creates a component that uses **configuration**, uses the **build method**, declares **internalised styles** and employs configuration bases style assignment:

```java
public class MyComponent extends Component<MyComponent.Config> {

    public static class Config extends Component.Config {

        public interface Style {

            public ILocalCSS styles();

            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }
                };
            }

            public static final Style NORMAL = create (NormalCSS.instance ());
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
            return (MyComponent) super.build (new MyComponent (this), data);
        }
    }

    public MyComponent(Config config) {
        super (config);
    }


    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // Build DOM structure.
        }).build ();
    }
    
    /********************************************************************
     * CSS
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IComponentCSS {
        // Add relevant styles.
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        ".../MyComponent.css"
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
}
```

#### Direct styles and CSS

Sometimes it is necessary to apply styles and CSS directly to the root element of an existing component. For components build by configuration this can be done with the `adorn(...)` method on `Component.Config`:

```java
...
ButtonCreator.$ (..., cfg -> {
    ...
    cfg.adorn (e -> CSS.MIN_WIDTH.apply (e, Length.em (6)));
    ...
});
...
```

#### Subclassing (extension)

When subclassing a component you can subclass the styles as well:

```java
package mycomponentpackage;

...

public class MyButton extends Button {

    public interface IMyLocalCSS extends Button.ILocalCSS {
        /* Declare any additional styles here */
    }

    /**
     * Strictly override the styles for the specific sub-class.
     */
    @Override
    protected IMyLocalCSS styles() {
        return MyLocalCSS.instance ();
    }

    /**
     * Use the desired button styles then add those specific to the
     * extension.
     */
    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/button/Button.css",
        "mycomponentpackage/MyButton.css"
    })
    public static abstract class MyLocalCSS implements IMyLocalCSS {

        private static MyLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (MyLocalCSS) GWT.create (MyLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
```

This approach effectively *extends* the styles for `Button` to accommodate the extra styles needed by `MyButton`. However, if the base component has multiple styles then this approach can become quite awkward (and stated above this extends only one of the base components styles and imposes a restriction to that). An alternative is [Complement](#subclassing-complement) existing styles.

#### Subclassing (complement)

Here we create a secondary set of styles:

```java
package mycomponentpackage;

...

public class MyButton extends Button {

    public interface IMyLocalCSS extends CssDeclaration {

        // This is a best-practice to help avoid clashes.
        public String scope();

        /* Declare any additional styles here */
    }

    /**
     * (Optional) convenience method to access the extra styles.
     */
    protected IMyLocalCSS extraStyles() {
        return MyLocalCSS.instance ();
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        // (Optional) Add in the scoping style (best practice).
        getRootEl().classList.add (extraStyles().scope());
    }

    /**
     * Use the desired button styles then add those specific to the
     * extension.
     */
    @CssResource({  
        "mycomponentpackage/MyButton.css"
    })
    public static abstract class MyLocalCSS implements IMyLocalCSS {

        private static MyLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (MyLocalCSS) GWT.create (MyLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
```

In this case a completely separate set of styles is created. These are accessible through a convenience method `extraStyles()` and can be applied anywhere exactly in the same manner as one would use for the base component styles via `styles()`.

Note the reference to scoping as a best-practice. This is an important consideration when a component can include other components and you want to avoid clashes. Otherwise it is less important as obfuscation of classes keeps class name unique.

### Rendering

#### Runtime rendering

This scenario applies when the component is responding to a change by manipulating its existing DOM without performing a re-render. Generally this means that a DOM node has been extracted and retained in the class instance and modifications to that element is being performed.

The following renders into an extracted element (the most common approach):

```java
public class MyComponent extends SimpleComponent {

    // Element extracted during rendering.
    private Element titleEl;

    ...

    public void updateTitle(String title, String description) {
        // This will clear the contents of the titleEl then build into
        // it the contents as rendered.
        buildInto (titleEl, el -> {
            H3.$ (el).text (title);
            P.$ (el).text (description);
        });
    }
}
```

This uses the `buildInto(...)` method on `Component` which will automatically register any declared event handlers (and replaces any from a prior call to this method made against the same element). If you are not declaring event handlers you could use the similarly named method on `Wrap` (which does not incur the overhead of registering event handlers):

```java
...
public void updateTitle(String title, String description) {
    Wrap.buildInto (titleEl, el -> {
        ...
    });
}
...
```

The following is a more comprehensive example that renders in response to a remote call (which is activated when the component is navigated to):

```java
public static class MyComponent extends SimpleComponent implements INavigationAware {
        
        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                // Empty content (will be updated)
            }).build();
        }

        protected void update(MyResult data) {
            buildInto (getRoot(), root -> {
                // Render out new contents.
            });
        }

        protected void loading() {
            buildInto (getRoot(), root -> {
                // Render out loading message.
            });
        }

        protected void failed() {
            buildInto (getRoot(), root -> {
                // Render out failure.
            });
        }
        
        @Override
        public void onNavigateTo(NavigationContext context) {
            // Perform a remote query that retuns an instance of
            // MyResults.
            ...
            .onBefore(() -> {
                loading ();
            })
            .onSuccess(result -> {
                update (data);
            })
            .onFailure(reason -> {
                failed();
            })
            ...

        }
    }
```

There are cases where you may want to select an element under another and render into that:

```java
public class MyComponent extends SimpleComponent {

    // Element extracted during rendering.
    private Element outerEl;

    ...

    public void updateTitle(String title, String description) {
        // This will find the first element contained in the passed
        // element (outerEl) that matches the given selector (#title).
        // It will then clear the contents of that element and build
        // into it the contents as rendered.
        Wrap.findAndBuildInto (outerEl, "#title", el -> {
            H3.$ (el).text (title);
            P.$ (el).text (description);
        });
    }
}
```

This won't register any event handlers, if you need to then you need to call the `registerEventHandler(...)` method directly:

```java
...
public void updateTitle(String title, String description) {
    registerEventHandler ((IUIEventHandler) Wrap.findAndBuildInto (outerEl, "#title", el -> {
        ...
    }), "some_unque_string_to_use_as_a_key");
}
...
```



### Node extraction

Extraction of nodes **without** using references. This is ideal for binding a node with some external object (and user behaviour).

```java
private Map<MyOption,Element> items = new HashMap<>();

@Override
protected INodeProvider buildNode(Element el) {
    List<MyOption> options = ... // Get options here.
    return Wrap.$ (el).$ (root -> {
        options.forEach (option -> {
            Div.$ (root).$ (item -> {
                item.apply (n -> items.put (option, (Element) n));
                item.onclick (e -> /* do something with option */);
                /* build out DOM */
            });
        });
    }).build ();
}
```

### Events

#### Previewing events

Preview events are invoked at the begining of the event loop and allows one to respond to the event, even cancelling it if required. This is often used for pop-up's to detect if a mouse event has occurred outside the bounds of the pop-up so that it can be closed (since such events would not be passed to the associated component).

```java
// The preview handler (need to remove it when done).
IEventPreviewHandler preview;

...

// Establish the preview.
preview = EventLifecycle.registerPreview (e -> {
    boolean cancel = false;

    /* Do something with the event and determine if it */
    /* needs to be cancelled.                          */

    if (cancel)
        return EventLifecycle.IEventPreview.Outcome.CANCEL;
    return EventLifecycle.IEventPreview.Outcome.CONTINUE;
});

...

// When done with the preview, remove it.
preview.remove ();
```

### Fragments

Recall that fragments only *contribute* to the build structure, which is built by the calling renderer. As such you should never directly invoke the `build(...)` method on any node builder; if you need to access the built DOM node you must use the `use(n -> {...})` method. However, there are no restrictions on declaring event handlers or making use of any of the node builder configuration methods.

#### Minimal (no children)

A minimal fragment using a renderer supplied by construction:

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent, /* Configuration */) {
        MyFrag frg = new MyFrag (/* Configuration */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public MyFrag(/* Configuration */) {
        super (parent -> {
            /* DOM content */
        });
    }
}
```

or by calling `builder(...)` (which allows access to instance methods and members):

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent /*, Configuration */) {
        MyFrag frg = new MyFrag (/* Configuration */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public MyFrag(/* Configuration */) {
        builder (parent -> {
            /* DOM content */
        });
    }
}
```

To apply adornments (such as when `css(...)` is used against the fragment):

```java
public MyFrag(/* Configuration */) {
    super (parent -> {
        Div.$ (parent).self (n -> adornments().adorn(n)).$ (
            /* DOM content */
        )
    });
}
```

or when using a fragment at the top level:

```java
public MyFrag(/* Configuration */) {
    super (parent -> {
        Stack.$ (parent).adorn (adornments() /*, additional adornments */).$ (
            /* DOM content */
        )
    });
}
```

#### Minimal (with children)

The pattern for fragments that have children is similar to the case without:

```java
public class MyFrag extends FragmentWithChildren<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent, /* Configuration */) {
        MyFrag frg = new MyFrag (/* Configuration */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public MyFrag(/* Configuration */) {
        super ((parent, children) -> {
            /* DOM content */
        });
    }
    
}
```

The same variations that apply to [Minimal (no children)](#minimal-no-children) apply to this case (i.e. use of the `builder(...)` method and application of adornments).

#### Build method (no children)

To employ the build method:

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /* Configuration methods */

    @Override
    public void buildInto(ElementBuilder parent) {
        /* Build DOM */
    }

}
```

Note that a parent node is created for the fragment and this not is passed to the `build(...)` methods. It is also this node that adornments are applied to. By default the root element is a DIV though you can change that by overriding the `createRoot(...)` method:

```java
protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
    // Change the root element accordingly.
    return P.$ (parent);
}
```

A more aggressive approach is to override the `build(...)` method directly which avoids creation of a root node and allows more than one child to be placed directly into the parent:

```java
public class MyFrag extends Fragment<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /* Configuration methods */

    @Override
    public void build(ContainerBuilder<?> parent) {
        /* Build DOM */
    }

}
```

However you will need to implement any conditional checking, adornment management (or any other standard support feature for fragments) yourself.

#### Build method (with children)

*The comments related to the creation of a parent and treatment of adornments for the non-child case apply equally for the child case.*

With children (the build method is for illustration so as to emphasise that you need to call the super method to include the children, for this you need to pass the target element):

```java
public class MyFrag extends FragmentWithChildren<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent) {
        MyFrag frg = new MyFrag ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /* Configuration methods */

    @Override
    public void buildInto(ElementBuilder parent) {
        Div.$ (parent).$ (childcontainer -> {
            /* Build DOM */
            // This adds each child into the child container.
            super.buildInto (childcontainer);
        });
    }

}
```

As with the non-children case you can override the `build(...)` method itself, in this case you have to access the children directly from the children member and process them separately:

```java
@Override
public void build(ContainerBuilder<?> parent) {
    Div.$ (parent).$ (childcontainer -> {
        /* Build DOM */
        children.forEach (child -> {
            // You have the flexibility to create richer containment structures.
            child.build (childcontainer);
        });
    });
}
```

#### Styling

The recommended approach for styling fragments is to create a non-conflicting CSS style sheet in the `public` directory of the module which is loaded in the initialiser (see [injected CSS](ess_styles.md#injected-css )). Fragments are then scoped by a unique class:

```java
public class MyFrag extends Fragment<MyFrag> {

    ...

    public MyFrag(Variant variant, /* Configuration */) {
        super (parent -> {
            Div.$(parent).style("fragMyFrag").$ (
                /* DOM content */
            );
        });
    }
}
```

with CSS:

```css
.fragMyFrag {
    ...
}
.fragMyFrag > div {
    ...
}
...
```

#### Variations

Sometimes you want to support variantions of a fragment (for example, a button may be text, outlined or full). This can be embodied by specifying the variations as a `enum` (or possibly multiple where you may have a variation in form and a variation in style):

```java
public class MyFrag extends Fragment<MyFrag> {

    public enum Variant {
        VARIANT1, VARIANT2, ...;
    }

    public static MyFrag $(IDomInsertableContainer<?> parent, Variant variant, /* Configuration */) {
        MyFrag frg = new MyFrag (/* Configuration */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public MyFrag(Variant variant, /* Configuration */) {
        super (parent -> {
            Div.$(parent).style("fragMyFrag", variant.name().toLowerCase()).$ (
                /* DOM content */
            );
        });
    }
}
```

Here the variant is applied as a style to the root element of the fragment (and should be declared in the CSS for the fragment).

#### Variations (by interface)

If the fragment is included in a library then you may want to allow users to provide additional customisation through their own variants. The `enum` approach does not allow for extension however replacing it with an interface is a way out:

```java
public class MyFrag extends Fragment<MyFrag> {

    public interface Variant {

        public final static VARIANT1 = Variant.create("variant1", ...);
        
        public final static VARIANT2 = Variant.create("variant2", ...);

        public String style();

        /* Other configuration */

        public static Variant create(String style, /* Other configuration */) {
            return new Variant() {
                public String style() { return style; }
                ...
            };
        }
    }

    public static MyFrag $(IDomInsertableContainer<?> parent, Variant variant, /* Configuration */) {
        MyFrag frg = new MyFrag (/* Configuration */);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public MyFrag(Variant variant, /* Configuration */) {
        super (parent -> {
            Div.$(parent).style("fragMyFrag", variant.style()).$ (
                /* DOM content */
            );
        });
    }
}
```

These behave nearly identically to the `enum` variety (so provides a migration pathway) but allows users to create their own variants and associated styles:

```java
public class MyFragVariants {
    public static final Variant MYVARIANT1 = Variant.create("myvariant1");
}
```

```css
.fragMyFrag.myvariant1 {
    ...
}
```

Where the variant can be applied as `MyFrag.$(parent, MyFragVariants.MYVARIANT1)`.

### Controls

#### Custom template (minimal)

#### Custom template (full)

This is a *fully dressed* component template that includes configuration and encapsulated styles (as well as customisable styles):

```java
public class XXXControl extends Control<T, XXXControl.Config> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

     /**
      * The default style to employ when one is not assign explicitly.
      */
     public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building an {@link XXXControl}.
     */
    public static class Config extends Control.Config<T, Config> {

        /**
         * Style for the control (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *                     the CSS styles.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            /**
             * Standard style.
             */
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * Assigns a different style.
         * 
         * @param style
         *              the style.
         * @return this configuration.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public XXXControl build(LayoutData... data) {
            return build (new XXXControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public XXXControl(XXXControl.Config config) {
        super (config);
    }

    /************************************************************************
     * Behaviour
     ************************************************************************/

    @Override
    protected T prepareValueForAssignment(T value) {
        // Any value preparation that needs to be applied (i.e. value cleaning).
        return super.prepareValueForAssignment (value);
    }

    @Override
    public T valueFromSource() {
        // Extract value from the DOM (or internal state).
        T value = ...;
        return value;
    }

    @Override
    public void valueToSource(T value) {
        // Assign value to internal state and update DOM.
    }

    /********************************************************************
     * Rendering.
     ********************************************************************/

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        // This could also be done in the constructor by using the
        // renderer(...) method.
        return Wrap.$ (el).$ (
            // Create structure DOM and event handlers.
        ).build (dom -> {
            // Extract any references that are needed.
        });
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    /**
     * Standard CSS for the control.
     */
    public static interface ILocalCSS extends IControlCSS {
        // Declare necessary styles.
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "mycontrolpackage/Control.css",
        "mycontrolpackage/XXXControl.css",
        // This is only needed when employing as a libary (for overrides).
        "mycontrolpackage/XXXControl_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
```

With the corresponding creator helper class:

```java
public class XXXControlCreator {

    /**
     * Convenience to build an instance of the control.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *            to configure the control.
     * @return the button instance.
     */
    public static XXXControl $(ContainerBuilder<?> el, Consumer<XXXControl.Config> cfg) {
        return With.$ (build (cfg), cpt -> el.render (cpt));
    }

    /**
     * Convenience to obtain a configuration.
     * 
     * @return the button configuration instance.
     */
    public static XXXControl.Config create() {
        return new XXXControl.Config ();
    }

    /**
     * Convenience to build an instance of the control.
     * 
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static XXXControl build(LayoutData...data) {
        return build (null, data);
    }

    /**
     * Convenience to build an instance of the control.
     * 
     * @param cfg
     *             to configure the control.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static XXXControl build(Consumer<XXXControl.Config> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    /**
     * Convenience to build an instance of the control.
     * 
     * @param cfg
     *                to configure the control.
     * @param applier
     *                to apply changes to the created control.
     * @param data
     *                (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static XXXControl build(Consumer<XXXControl.Config> cfg, Consumer<XXXControl> applier, LayoutData...data) {
        XXXControl.Config config = new XXXControl.Config ();
        if (cfg != null)
            cfg.accept (config);
            XXXControl ctl = config.build (data);
        if (applier != null)
            applier.accept (ctl);
        return ctl;
    }
}
```

## Modal dialogs

### Inline modals

An inline modal that wraps and processes a (in this case a form) component. Note the generic types `Void`; these can be replaced by types as required.

```java
ModalDialogCreator.build (ControlFormCreator.<Void,Void> build (cfg -> ControlFormCreator.configureForDialog (cfg), form -> {
    // Form with a notice and a single field that captures an email address.
    form.component (ComponentCreator.build (cpt -> {
        Notice.$ (cpt).message ("There is no email address, please provide one");
    }), cell -> cell.grow (1));
    form.control ("email", "", TextControlCreator.build (cfg -> {
        cfg.placeholder ("Email address");
    }, ctl -> ctl.setValue (r.getEmail ())), cell -> {
        cell.grow (1);
    });
}), mcfg -> {
    // Removes the dialog completely when closed.
    mcfg.removeOnClose ();

    // Title and width.
    mcfg.title ("Send to email");
    mcfg.width (Length.px (400));

    // Close action, default action closes the dialog.
    mcfg.action (action -> {
        action.label ("close").link ();
    });

    // Send action that validates the form and performs the action.
    mcfg.action (action -> {
        action.label ("Send").handler (form -> {
            if (!form.contents ().validate ()) {
                form.fail ();
                return;
            }
            // Perform action
            form.success ();
        });
    });
}).open ();
```

### Modal-enabling a component

Here we consider a component that can be used directly or as a model. The latter is invoked by a static `open(...)` method.

#### Simple dialog

```java
public static class MyComponent extends SimpleComponent {

    private static IDialogOpener<Void, Void> DIALOG;

    public static void open() {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Void, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create something")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create something"));
        DIALOG.open (null, null);
    }

    public MyComponent() {   
        ...
    }
}
```

#### Processing dialog

This makes use of `IProcessable` to manage a response that is returned through to a callback:

1. If the dialog is cancelled the optional value is empty.
2. If the dialog is processed then optional value contains the returned value from the `process(...)` method.
3. Passing an empty optional in the `process(...)` method indicates a failure of the processing and assumes the some form of error message is being displayed; the dialog is not closed.

```java
public static class MyComponent extends SimpleComponent implements IProcessable<ResponseType> {

    private static IDialogOpener<Void, ResponseType> DIALOG;

    public static void open(Consumer<Optional<ResponseType>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, ResponseType, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create something")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create something"));
        DIALOG.open (null, cb);
    }

    public MyComponent() {   
        ...

        @Override
        public void process(Consumer<Optional<ResponseType>> outcome) {
            ResponseType response = ...;

            if (response == null)
                // This keeps the dialog open.
                outcome.accept(Optional.empty());
            else
                // While this closes the dialog ane
                outcome.accept(Optional.of(response));
        }
    }
}
```

#### Configurable dialog

Activates the dialog passing configuration data through to the component. This can be combined with [Processing dialog](#processing-dialog).

```java
public static class MyComponent extends SimpleComponent implements IEditable<ConfigData> {

    private static IDialogOpener<ConfigData, Void> DIALOG;

    public static void open(ConfigData config) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<ConfigData, Void, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create something")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create something"));
        DIALOG.open (config, null);
    }

    public MyComponent() {   
        ...

        @Override
        public void edit(ConfigData config) {
            ...
        }
    }
}
```

Note that if the component extends `ControlForm<ConfigData,?>` then `edit(ConfigData)` is provided by the base class and will be used to populate the form contents.

It is common to declare `ConfigData` as a record:

```java
public static class MyComponent extends SimpleComponent implements IEditable<ConfigData> {

    public static record ConfigData(...) {
        public static ConfigData of(...) {
            return new ConfigData(...);
        }
    }

    ...
}
```

One then opens the dialog as (or similar):

```java
MyComponent.open(MyComponent.ConfigData.of(...));
```

The `of(...)` method actually represents a family of methods that take varying arguments for different configuration arrangements.

#### Secondary configuration

The means of configuration described above (primary configuration) is often tied to the contents the diaglog is to display. Sometimes you want to configure the dialog in a manner secondary to the primary (i.e. the primary is used with `IEditable` but you want to set some scope).

The recommended approach is to access the contents directly in the `open` support method:

```java
public static class MyComponent extends SimpleComponent implements IEditable<MyEditable> {

    private static IDialogOpener<MyEditable, Void> DIALOG;

    public static void open(MyContext context, MyEditable editable) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<MyEditable, Void, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Update something")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Update something"));
        // Secondary configuration.
        ((MyComponent) DIALOG.dialog().contents()).context(context);
        DIALOG.open (editable, null);
    }

    public MyComponent() {   
        ...

        public void context(MyContext context) {
            ...
        }

        @Override
        public void edit(MyEditable editable) {
            ...
        }
    }
}
```

### Modal with resolver

It is quite common to pop-up a modal, have it retrieve some data then present that data. In this context it makes sense to use an `IResolver`:

```java
public class MyComponent extends SimpleComponent implements IEditable<MyResult> {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    private static IDialogOpener<IResolver<MyResult>, Void> DIALOG;

    public static void open(IResolver<MyResult> resolver) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<IResolver<MyResult>, Void, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Preview")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, null, b -> b.label ("Close"));
        DIALOG.open (resolver, null);
    }

    /************************************************************************
     * Main class.
     ************************************************************************/

    private MyResult result;

    private boolean failed;

    public MyComponent() {
        renderer(root -> {
            if (result != null) {
                // Display content.
                ...
            } else if (failed) {
                // Display error.
                ...
            } else {
                // Display loading.
                ...
            }
        });
    }

    @Override
    public void editLoading() {
        this.result = null;
        this.failed = false;
        rerender();
    }

    @Override
    public void editFailed() {
        this.failed = true;
    }

    @Override
    public void edit(AssetClassLookupResult result) {
        this.result = result;
        this.failed = false;
        rerender();
    }
    
}
```

### Modal with state

As an alternative to using a [resolver](#modal-with-resolver) we can make use of a state:

```java
public class MyComponent extends StateComponent<LocalState> implements IEditable<Long> {
    
    ...

    public MyComponent() {
        super(new LocalState());
        renderer(root -> {
            if (state().isOK()) {
                // Display data.
            } else if (state().isLoading()) {
                // Loading.
            } else {
                // Error.s
            }
        });
    }

    @Override
    public void edit(Long id) {
        state().load(id);
    }

    /************************************************************************
     * State.
     ************************************************************************/

    static class LocalState extends LifecycleStateVariable<LocalState> { 

        protected MyResult result;

        protected void load(long id) {
            loading();
            // Load the data; this is an example using RPC.
            new MyServiceHandler<MyResult> ()
            .onSuccessful (v -> {
                this.result = v;
                modify();
            })
            .onFailure ((v, s) -> {
                error(v, w -> w.getMessage());
            })
            .remoteExecute (MyLookup.byId (id));
        }
    }
}
```

This has the advantage of making available details on the error as well as providing a standardised lifecycle.

### Modal with open awareness

There are cases where you want the modal contents to perform some action on open but you don't want to pass it any initialisation or configuration data. Modals respect when their contents implement `IOpenAware` invoking the `onOpen()` method when opened.

```java
public class MyComponent extends SimpleComponent implements IOpenAware {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    private static IDialogOpener<Void, Void> DIALOG;

    public static void open() {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Void, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Preview")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Update"));
        DIALOG.open (null, null);
    }

    /************************************************************************
     * Main class.
     ************************************************************************/
    
    ...

    @Override
    public void onOpen() {
        // This is invoked when the modal is opened.
        // Perform any loading and rendering needed on open.
        ...
    }
}
```

### Modal with internal action

Sometimes you want the modal contents to initiate the required action (e.g. selecting from a range of options) without needed to have the user select a separate action button. Modals respect contents that fire `IValueChangeListener.onValueChanged()` events.

```java
public class MyComponent extends SimpleComponent implements IOpenAware {

    public enum Option {
        OPTION1, OPTION2;
    }

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    private static IDialogOpener<Void, Option> DIALOG;

    public static void open(Consumer<Optional<Option>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Option, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Preview")
                    .type (Type.CENTER)
                    .width (Length.px(500));
            }, b -> b.label ("cancel"), null);
        DIALOG.open (null, cb);
    }

    /************************************************************************
     * Main class.
     ************************************************************************/
    
    ...

    public MyComponent() {
        renderer(root -> {
            A.$(root).text("selection option 1")
                .onclick(e -> {
                    MyComponent.this.fireEvent(IValueChangeListener.class).onValueChanged(Option.OPTION1);
                });
            A.$(root).text("selection option 2")
                .onclick(e -> {
                    MyComponent.this.fireEvent(IValueChangeListener.class).onValueChanged(Option.OPTION2);
                });
        });
    }
}
```

The modal adds a listener and when the event is detected it passes the value back through the callback then closes the dialog (the same behaviour applied here as it does to the `IProcessable` case, namely the returned valued cannot be `null`). Note the `null` passed for the action button, this prevents the action form displaying.

If you don't want the dialog to close, then fire an `IUpdateListener.update(...)` event. The modal will pass back the value (if not `null`) but will not close the dialog.

If you want to close the dialog programmatically, modals will respond to the `ICloseListener.onCloseRequested()` event from the contents and will close the dialog.

## Form patterns

The following form patterns make use of the `ControlForm<SRC,DST>` base class where `SRC` is the type use to popluate the form and `DST` is the type used to apply form changes to (typically a *command*, see [Remoting](#remoting)).

### Create form

#### Form component

For the following template the create form exends `ControlForm<SRC,DST>` with:

1. `SRC` being set to `Void` to reflect that no pre-population is being performed.
2. `DST` being set to `XYZCommand` which represents a command object to be populated by the form and used to remotely create the associated entity.
3. Implements `IProcessable<Long>` which dictates the processing mechanism whose response type is a `Long` that confirms to the ID of the created entity (change the type if the ID type is different).

```java
public class XYZCreateForm extends ControlForm<Void,XYZCommand> implements IProcessable<Long> {

    /**
     * Construct an instane of the form panel.
     */
    protected XYZCreateForm() {
        super (ControlFormCreator.createForDialog ());

        group (grp -> {
            grp.control ("title", "Position title", Controls.text (cfg -> {
                cfg.acceptor ("title").validator (Validators.notEmpty ("please enter a title"));
            }), cell -> cell.grow (1).required ());
        });
    }

    @Override
    public void process(Consumer<Optional<Long>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        // XYZCommand is some object that serves to capture data for remoting.
        String title = value("title");
        XYZCommand cmd = apply (
            new XYZCommand (new XYZCommand.Construct(title, ...))
        );

        // Choose the desired remote call here. This assumes you are using the
        // JUI remoting framework with handler MyServiceHandler. 
        new MyServiceHandler<ResolutionsResult> ()
            .onSuccessful (v -> {
                // The first (and only) element of the resolutions result is
                Notifier.create ().text ("Successfully created").show (2000);
                outcome.accept (v.firstAsLong ());
            })
            .onFailure ((v, s) -> {
                // Failure delivers an empty outcome.
                XYZCreateForm.this.invalidate (v);
                outcome.accept (Optional.empty ());
            })
            .remoteExecute (new ResolutionsResultLookup(), cmd);
    }

}
```

If you wanted to return fully populate result of the resulting entity then replace `Long` in `IProcessable` with the DTO type and modify the `process(...)` method accordingly:

```java
public class XYZCreateForm extends ControlForm<Void,XYZCommand> implements IProcessable<XYZLookupResult> {

    /**
     * Construct an instane of the form panel.
     */
    protected XYZCreateForm() {
        super (ControlFormCreator.createForDialog ());

        group (grp -> {
            grp.control ("title", "Position title", Controls.text (cfg -> {
                cfg.acceptor ("title").validator (Validators.notEmpty ("please enter a title"));
            }), cell -> cell.grow (1).required ());
        });
    }

    @Override
    public void process(Consumer<Optional<XYZLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        // XYZCommand is some object that serves to capture data for remoting.
        String title = value("title");
        XYZCommand cmd = apply (
            new XYZCommand (new XYZCommand.Construct(title, ...))
        );

        // Choose the desired remote call here.
        new MyServiceHandler<XYZLookupResult> ()
            .onSuccessful (v -> {
                // The first (and only) element of the resolutions result is
                Notifier.create ().text ("Successfully created").show (2000);
                outcome.accept (v);
            })
            .onFailure ((v, s) -> {
                // Failure delivers an empty outcome.
                XYZCreateForm.this.invalidate (v);
                outcome.accept (Optional.empty ());
            })
            .remoteExecute (XYZLookup.byUniqueReference (cmd.reference()), cmd);
    }

}
```

Note that the form does not include any action buttons and the expectation is that to process the form one executes the `process(...)` method and responds to the callback outcome. If this is empty then the form should be considered a failure while a value means success (so can be navigated away from):

```java
myForm.process(outcome -> {
    if (outcome.isEmpty()) {
        // Do nothing as the form is in error.
        ...
    } else {
        // Navigate away or close the form and do something with the result.
        var result = outcome.get();
        ...
    }
});
```

Finally you may not want to return either the ID of the created entity or a representation of it. The simplest approach then is to employ an `Object`:

```java
public class XYZCreateForm extends ControlForm<Void,XYZCommand> implements IProcessable<Object> {

    ...

    @Override
    public void process(Consumer<Optional<Object>> outcome) {
        ...

        new MyServiceHandler<Void> ()
            .onSuccessful (v -> {
                // The first (and only) element of the resolutions result is
                Notifier.create ().text ("Successfully created").show (2000);
                outcome.accept (new Object());
            })
            .onFailure ((v, s) -> {
                // Failure delivers an empty outcome.
                XYZCreateForm.this.invalidate (v);
                outcome.accept (Optional.empty ());
            })
            .remoteExecute (cmd);
    }

}
```

Here the general semantics of processing remain the same (an empty optional means failed processing) so the object is just used to distinguish from empty.

#### Opening in a dialog

The form above can be modified so that it operates as a dialog. Note that the dialog makes use of the fact the form implements `IProcessable` meaning that the callback (of type `Consumer<Optional<Long>>`) will be passed through to the `process(...)` method.

```java
public class XYZCreateForm extends ControlForm<Void,XYZCommand> implements IProcessable<Long> {

    private static IDialogOpener<Void, Long> DIALOG;

    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Long, XYZCreateForm>dialog (new XYZCreateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create an XYZ")
                    .type (Type.CENTER)
                    .width (Length.px (500));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Create XYZ"));
        DIALOG.open (null, cb);
    }

    // Below is as per the previous example above.
    ...

}
```

To action:

```java
XYZCreateForm.open(outcome -> {
    ...
});
```

This can be called from anyware as supports layering of dialogs.

#### Bare-bones template

A bare-bones version of the above:

```java
public class XYZCreateForm extends ControlForm<Void,XYZCommand> implements IProcessable<XYZLookupResult> {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    private static IDialogOpener<Void, Long> DIALOG;

    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Long, XYZCreateForm>dialog (new XYZCreateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create an XYZ")
                    .type (Type.CENTER)
                    .width (Length.px (500));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Create XYZ"));
        DIALOG.open (null, cb);
    }

    /************************************************************************
     * Form implementation.
     ************************************************************************/

    protected XYZCreateForm() {
        super (ControlFormCreator.createForDialog ());

        // Build form
    }

    @Override
    public void process(Consumer<Optional<XYZLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        // Process
    }

}
```

### Update form

#### Form component

For the following template the create form exends `ControlForm<SRC,DST>` with:

1. `SRC` being set to `XZYLookupResult` that contains all the information being updated.
2. `DST` being set to `XYZCommand` which represents a command object to be populated by the form and used to remotely update the associated entity.
3. Implements `IProcessable<XZYLookupResult>` which dictates the processing mechanism whose response type is a re-populated lookup result containing the changes.

```java
public class XYZUpdateForm extends ControlForm<XZYLookupResult,XYZCommand> implements IProcessable<XZYLookupResult> {

    /**
     * Construct an instane of the form panel.
     */
    protected XYZUpdateForm() {
        super (ControlFormCreator.createForDialog ());

        group (grp -> {
            grp.control ("title", "Position title", Controls.text (cfg -> {
                cfg.acceptor ("title").validator (Validators.notEmpty ("please enter a title"));
            }), cell -> {
                cell.grow (1).required ();
                // This extracts a value from XZYLookupResult and returns that value
                // which is then assigned to the control.
                cell.from(v -> v.getTitle());
                // This takes the value v of the control and applies to to cmd the
                // instance of XYZCommand. This only occurs if the control is dirty
                // (it was updated by the user).
                cell.to((ctx,v,cmd) -> cmd.title(v));
            });
        });
    }

    @Override
    public void process(Consumer<Optional<XZYLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        // XYZCommand is some object that serves to capture data for remoting.
        // Passing to apply allows the cells in the form to populate the object
        // (i.e. see cell.to above).
        XYZCommand cmd = apply (new XYZCommand ());

        // Choose the desired remote call here. This assumes you are using the
        // JUI remoting framework with handler MyServiceHandler. 
        new MyServiceHandler<XZYLookupResult> ()
            .onSuccessful (v -> {
                // The first (and only) element of the resolutions result is
                Notifier.create ().text ("Successfully updated").show (2000);
                outcome.accept (v);
            })
            .onFailure ((v, s) -> {
                // Failure delivers an empty outcome.
                XYZUpdateForm.this.invalidate (v);
                outcome.accept (Optional.empty ());
            })
            // The call to source() returns the lookup instance used to
            // populate the form.
            .remoteExecute (XZYLookup.byId(source().getId()), cmd);
    }

}
```

To populate the form you must call `edit(...)`:

```java
XZYLookupResult result = ...;
xyzUpdateForm.edit(result);
```

Procesing is as per the create form (calling `process(...)`).

#### Opening in a dialog

Similarly to the create form the update form can be turned unto a dialog:

```java
public class XZYUpdateForm extends ControlForm<XZYLookupResult,XYZCommand> implements IProcessable<XZYLookupResult> {

    private static IDialogOpener<Void, XZYUpdateForm> DIALOG;

    public static void open(XZYLookupResult resultToUpdate, Consumer<Optional<XZYLookupResult>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<XZYLookupResult, XZYLookupResult, XZYUpdateForm>dialog (new XZYUpdateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Update an XYZ")
                    .type (Type.CENTER)
                    .width (Length.px (500));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Update"));
        DIALOG.open (resultToUpdate, cb);
    }

    // Below is as per the previous example above.
    ...

}
```

Note that passed is an instance of `XZYLookupResult` which contains the data to pre-populate the form:

```java
XZYLookupResult result = ...;
XZYUpdateForm.open(result, outcome -> {
    ...
});
```

This can be called from anyware as supports layering of dialogs.

#### Bare-bones template

A bare-bones version of the above:

```java
public class XYZUpdateForm extends ControlForm<XYZLookupResult,XYZCommand> implements IProcessable<XYZLookupResult> {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    private static IDialogOpener<XYZLookupResult, XYZLookupResult> DIALOG;

    public static void open(Consumer<Optional<XYZLookupResult>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<XYZLookupResult, XYZLookupResult, XYZUpdateForm>dialog (new XYZUpdateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create an XYZ")
                    .type (Type.CENTER)
                    .width (Length.px (500));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Create XYZ"));
        DIALOG.open (null, cb);
    }

    /************************************************************************
     * Form implementation.
     ************************************************************************/

    protected XYZUpdateForm() {
        super (ControlFormCreator.createForDialog ());

        // Build form
    }

    @Override
    public void edit(QuestionLookupResult result) {
        // Any form adjustments

        super.edit(result);
    }

    @Override
    public void process(Consumer<Optional<XYZLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        // Process
    }

}
```

### Update extends create

The previous patterns kept update and create separate. In particular the building of the form controls is replicated in both (generally one would expect there to be differences, but often these are not significant). A more effient approach is to build out the form in one class then have the other class extend the first.  For forms the best approach is to have the update extend the create:

```java
public class XYZCreateForm extends ControlForm<XYZLookupResult,XYZCommand> implements IProcessable<XYZLookupResult> {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    /**
     * See {@link #open()}.
     */
    private static IDialogOpener<Void, XYZLookupResult> DIALOG;

    /**
     * Opens an instance of the panel in a dialog.
     * 
     * @param cb
     *           the callback.
     */
    public static void open(Consumer<Optional<XYZLookupResult>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, XYZLookupResult, XYZCreateForm>dialog (new XYZCreateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Create XYZ")
                    .type (Type.SLIDER)
                    .width (Length.px (625));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Create XYZ"));
        DIALOG.open (null, cb);
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Construct an instane of the create form.
     */
    public XYZCreateForm() {
        this(true);

        handleAfterRender(() -> {
            ...
        });
        handleReset(ctx -> {
            ...
        });
    }

    /**
     * Common constructor.
     */
    protected XYZCreateForm(boolean create) {
        super (ControlFormCreator.createForDialog ());

        // Build out the form. Used "create" to control what elements are created and where.
    }

    /************************************************************************
     * Lifecycle.
     ************************************************************************/

    @Override
    public void process(Consumer<Optional<XYZLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }
        ...
    }

}
```

Note that the `SRC` parameter is assign to that needed for update, which is fine since the create form does not partake in editing so can be ignored. The benefit is only conveyed to the update form as an extension:

```java
public class XYZUpdateForm extends XZYCreateForm {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    /**
     * See {@link #open()}.
     */
    private static IDialogOpener<XYZLookupResult, XYZLookupResult> DIALOG;

    /**
     * Opens an instance of the panel in a dialog.
     * 
     * @param cb
     *           the callback.
     */
    public static void open(XYZLookupResult resultToUpdate, Consumer<Optional<XYZLookupResult>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<XYZLookupResult, XYZLookupResult, XYZUpdateForm>dialog (new XYZUpdateForm (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                    .title ("Update XZY")
                    .type (Type.SLIDER)
                    .width (Length.px (650));
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Update"));
        DIALOG.open (resultToUpdate, cb);
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Construct an instane of the form panel.
     */
    protected XYZUpdateForm() {
        super(false);

        handleAfterRender(() -> {
            ...
        });
        handleReset(ctx -> {
            ...
        });
    }

    /************************************************************************
     * Lifecycle.
     ************************************************************************/

    @Override
    public void edit(XYZLookupResult result) {
        // Adjust the form as needed.

        super.edit(result);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IProcessable#process(java.util.function.Consumer)
     */
    @Override
    public void process(Consumer<Optional<XYZLookupResult>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }

        ...
    }

}
```

The form construction code then resides in the create base class.

## State

### Value state

In lieu of passing a state-variable around, the *notifier pattern* provides for a centralised `StateVariable` that can be accessed from anywhere.

```java
// Declare the variable with an `instance()` accessor.
public class NameNotifier extends ValueStateVariable<String> {

    private final static NameNotifier INSTANCE = new NameNotifier();

    public static NameNotifier instance() {
        return INSTANCE;
    }
}
```

The notifier can be accessed directly to notify of a change:

```java
updateBtn = ButtonCreator.build(cfg -> {
    cfg.label("Update");
    cfg.handler(() -> {
        String name = /* perform name update */
        OrganisationNameNotifier.instance().assign(name);
    });
});
```

Listeners can then respond to those changes:

```java
NameNotifier.instance().listen(state -> {
    String name = state().value();
    /* update the UI to reflect the change in name */
});
```

The state can be passed through to a state-component:

```java
public class NameComponent extends StateComponent<NameNotifier> {

    public CountIndicator() {
        super(NameNotifier.instance());
    }

    ...

}

```

### Inlined state

A state component will rerender completely on change of state, however, you may only want a portion to rerender. This can be achived by inlining a state:

```java
public class MyComponent extends SimpleComponent {

    private MyState state = new MyState();

    public MyComponent() {
        renderer(root -> {
            Div.$(root).$(inner -> {
                Div.$(inner).style("panel1").$(...);
                StateComponentBuilder.$(inner, (s,el) -> {
                    el.style("panel2");
                    ...
                });
                Div.$(inner).style("panel3").$(...);
            });
        });
    }
}
```

Here the state component is styles as `panel2` so can be viewed as simply part of the DOM of the parent component as so can make use of styles declared from the parent. It will also respond to state changes as a state component is expected to without affecting the surrounding DOM.

### Scroll preservation

During re-render, if there is a rending of an interim state (such a loading indicator) then any prior scroll position, that may want to be preserved, will likely be lost. In this case, scroll state can be preserved with `ScrollPreserver`.

```java
public class MyComponent extends SimpleComponent {

    private MyState state = new MyState();

    private ScrollPreserver scroll;

    public MyComponent() {
        renderer(root -> {
            Div.$(root).$(inner -> {
                Div.$(inner).style("panel1").$(...);
                StateComponentBuilder.$(inner, (s,el) -> {
                    el.style("panel2");
                    el.by("root");
                    if (state.isLoading()) {
                        ...
                    } else {
                        ...
                    }
                }, (s,dom) -> {
                    if (scroll == null)
                        scroll = new ScrollPreserver(dom.first("root"));
                    else
                        scroll.restore();
                });
                Div.$(inner).style("panel3").$(...);
            });
        });
    }

    protected void action(...) {
        scroll.preserve();
        ...
        // For example: force a state change and subsequent re-render of intermin state.
        state().reload();
    }
}
```

## JUI components

These are the standard components that come with JUI. Most are fairly straight forward but others (such as tables) require more complex configuration.

### Table

#### Prototyping store

When prototyping it is advantageous to use a mock store that is pre-populated and configured to behave like a remote store. This can be achieved by extending `ListPaginatedStore`:

```java
public class MyResultStore extends ListPaginatedStore<MyResult> {

        @Override
        protected void populate(List<MyResult> records) {
            MyResult result;

            result = new MyResult ();
            // Assign mock data to the result.
            records.add (result);

            // Repeat above for more results so as to build out
            // the backing data.
        }

    }
```

#### Table creator

To create a table inline into a parent DOM container:

```java
IStore<R> store = ...;
...
TableCreator.<R>$ (parent, cfg -> {

    // Configure the headers.
    cfg.header ("Heading 1", header -> {
        header.renderer (TextTableCellRenderer.create(r -> r.getCustomer()));
        header.sortable (SortDirection.ASC, s -> Logger.log ("Heading 1 " + s.name ()));
        header.width (Length.em (5));
    });
    cfg.header ("Heading 2", header -> {
        header.renderer (LinkTableCellHandler.create(r -> r.getCustomer(), r -> {
            Logger.log ("Clicked on cell: " + r.getCustomer());
        }));
        header.sortable (SortDirection.DESC, s -> Logger.log ("Heading 2 " + s.name ()));
        header.sorted (true);
        header.width (Length.em(10));
    });
    
    // Configure the empty content (when filtering).
    cfg.emptyFiltered (not -> {
        not.panel (p -> {
            p.title ("Nothing to show");
            p.paragraph ("No results found for the filter options you have choosen.");
            p.action ("Clear filters", () -> {
                store.clearAndReload ();
            });
            p.actionsRightAligned (true);
        });
    });

    // Configure the empty content (no filtering).
    cfg.emptyUnfiltered (not -> {
        not.panel (p -> {
            p.title ("Nothing available");
            p.paragraph ("There is nothing to show.");
            p.actionsRightAligned (true);
        });
    });

}, store);
```

#### Cell renderers

Using a `BuilderTableCellRenderer` to render fragments (with event handling):

```java
header.renderer (BuilderTableCellRenderer.create ((e,r) -> {
    Action.$ (e, FontAwesome.cloud(), "download", () -> {
        Logger.log ("DOWNLOAD");
    });
}));
```

#### Reload on navigation

When a table appears in a page it makes sense to reload the contents of the table whenever the page is navigated to. This ensures that the data displayed is as relevant as possible. So long as the page appears in some form of navigation structure (i.e. a `TabNavigator` or some custom navigation component) this can be achieved by having the page component implement `INavigationAware` and acting on a call to `onNavigateTo(...)`.

```java
public class MyComponent extends ... implements INavigationAware {
    ...
    @Override
    public void onNavigateTo(NavigationContext context) {
        store.reload (10);
    }
}
```

Note that you should always set the reload size. It is possible that the store could be loaded up with many pages and a reload without a page size will attempt to reload all records. This can be expensive so resetting the page size is a sensible strategy.

As an aside `onNavigateTo(...)` may be called **before** the page has rendered (this is common for the card-fit-layout used by most panels that does not render a component until it is activated). If the table is only instantiated on render then the table will not receive the initial store events. As a default the table will render what is in the store upon render so this is normally not a problem unless you rely on this behaviour for other purposes. If you do then you can extend the above to load on navigation and on render:

```java
public class MyComponent extends ... implements INavigationAware {
    ...
    @Override
    public void onNavigateTo(NavigationContext context) {
        if (!isRendered ())
            store.reload (10);
    }

    @Override
    public void onAfterRender () {
        store.reload (10);
    }
}
```

### Gallery

```java
```

## Remoting

The following pertains to the remoting implementation provided by `jui-remoting`.

### Adding a new remote

Assumes you have it setup to scan, then:

1. Create the query / lookup class.
2. Create the result class.
3. Create the processor.

Then restart the application server and clear the code server cache. It should work immediately.

### Transfer classes

Recall that transfer classes need to:

1. Extend `Result` (or be annotated with `@JsonSerialisable` at a minimum).
2. Be scanned in the remoting endpoint if being set to the server.

Also recall that when you create a new remoting class and you are using the [Code Server](app_codeserver.md) then you must clear the [server cache](app_codeserver.md#clear-cache) to ensure the class is registered with the serialisation/deserialisation mechanism.

Standard family of transfer classes:

**Base result (for type)**
```java
package myapplication.remoting.dto;

import com.effacy.jui.rpc.handler.client.query.Result;

public class MyResult extends Result {

    // Properties including setters and getters here.

}
```

**Result set query**
```java
package myapplication.remoting.dto;

public class MyQueryResult extends MyResult {

    // Properties including setters and getters here.

}
```

**Result set over query**
```java
package myapplication.remoting.dto;

import com.effacy.jui.rpc.handler.client.IConverter;
import com.effacy.jui.rpc.handler.client.query.ResultSet;

public class MyQueryResultSet extends extends ResultSet<MyQueryResult> {

    /**
     * Empty result set.
     */
    public MyQueryResultSet() {
        super (null, 0);
    }

    /**
     * Constructs the result set.
     * 
     * @param results
     *            the results as an iterable.
     * @param totalResults
     *            the total number of results (that would be returned without
     *            pagination) which may be negative and the total will be taken
     *            as the size of the results.
     */
    public MyQueryResultSet(Iterable<MyQueryResult> results, int totalResults) {
        super (results, totalResults);
    }

    /**
     * Constructs the result set.
     * 
     * @param results
     *            the results as an iterable over the source class.
     * @param converter
     *            the converter to convert from the source class to the return
     *            type class for this result set.
     * @param totalResults
     *            the total number of results (that would be returned without
     *            pagination) which may be negative and the total will be taken
     *            as the size of the results.
     */
    public <S> MyQueryResultSet(Iterable<S> results, IConverter<S, MyQueryResult> converter, int totalResults) {
        super (results, converter, totalResults);
    }
}
```

**Result set query**

The below includes (for illustration) fields for sorting and keyword search.

```java
public class MyQuery extends PageQuery<MyQueryResultSet> {

    public enum Sort {
        DIMENSION_ASC, DIMENSION_DESC;
    }

    private Sort sort;

    private String keywords;
    
    /**
     * Serialisation constructor.
     */
    protected MyQuery() {
        super ();
    }

    /**
     * Construct with pagination.
     * 
     * @param page
     *            the page.
     * @param pageSize
     *            the page size.
     */
    public MyQuery(int page, int pageSize) {
        super (page, pageSize);
    }

    /**
     * Assigns a sorting direction.
     * 
     * @param sort
     *             the direction.
     * @return this query instance.
     */
    public MyQuery sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Assign search keywords.
     * 
     * @param keywords
     *                 the keywords.
     * @return this instance.
     */
    public MyQuery keywords(String keywords) {
        setKeywords (keywords);
        return this;
    }

    /**
     * Serialisation.
     */
    public Sort getSort() {
        if (sort == null)
            sort = Sort.DIMENSION_ASC;
        return sort;
    }

    /**
     * Serialisation.
     */
    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public boolean filtering() {
        // We ignore specification of user as that is likely to be a pivot.
        if (!StringSupport.empty (keywords))
            return true;
        return super.filtering ();
    }

}
```

**Result reference**
```java
public class MyRef extends Ref {

    /**
     * Construct with an ID.
     * 
     * @param id
     *           the ID of the position.
     * @return the lookup.
     */
    public static MyRefById byId(long id) {
        return With.$ (new MyRefById(), v -> v.setId(id));
    }

    /************************************************************************
     * Reference classes.
     ************************************************************************/
    
    public static class MyRefById extends MyRef {
        /**
         * See {@link #getId()}.
         */
        private Long id;

        /**
         * Serialisation.
         */
        public Long getId() {
            return id;
        }

        /**
         * Serialisation.
         */
        public void setId(Long id) {
            this.id = id;
        }
    }
}
```

**Lookup result**
```java
public class MyLookupResult extends MyResult {

}
```

**Lookup**
```java
public class MyLookup extends Lookup<MyLookupResult,MyRef> {

    /**
     * Construct using {@link MyRefById}.
     */
    public static MyLookup byId(long id) {
        return new MyLookup(MyRef.byId(id));
    }

    /************************************************************************
     * Class body
     ************************************************************************/

    /**
     * Serialisation constructor.
     */
    protected MyLookup() {
        super();
    }

    MyLookup(ReviewRef lookup) {
        super(lookup);
    }
}
```

**Command**
```java
public class MyCommand extends C {

    /**
     * See {@link #title(String)}.
     */
    private VString title = new VString();

    /************************************************************************
     * Construction.
     ************************************************************************/

    protected MyCommand() {}

    public MyCommand(MyRef lookup) {
        super(lookup);
    }

    public MyCommand(MyConstruct construct) {
        super(construct);
    }

    public static class MyConstruct extends Construct {

        private String title;

        protected MyConstruct() {}

        public MyConstruct(String title) {
            this.title = title;
        }

        /**
         * Serialisation.
         */
        public String getTitle() {
            return title;
        }
        
        /**
         * Serialisation.
         */
        public void setTitle(String title) {
            this.title = title;
        }

    }

    /************************************************************************
     * Properties modifiers.
     ************************************************************************/

    /**
     * Assigns a new title.
     * 
     * @param title
     *              the title.
     * @return this command instance.
     */
    public MyCommand title(String title) {
        assign (this.title, title);
        return this;
    }

    /************************************************************************
     * Serialisation.
     ************************************************************************/

    /**
     * Serialisation.
     */
    public VString getTitle() {
        return title;
    }

    /**
     * Serialisation.
     */
    public void setTitle(VString title) {
        this.title = title;
    }
}
```

### State components

```java
new MyServiceHandler<MyResult>()
    .onSuccessful(v -> {
        state().assign(v);
    })
    .onFailure((errors,t) -> {
        state().error(errors, v -> v.getMessage (), t.name ());
    })
    .onBefore(v -> {
        state().loading();
    })
    .remoteExecute(new MyResultQuery());
```

### Result-set query

This assumes one is employing the standard processor pattern as described in [Remoting](topic_remoting.md).

#### Transfer classes

**Result class** which carries individual item data:
```java
import com.effacy.jui.rpc.handler.client.query.Result;

public class MyQueryResult extends Result {

    // Add properties
}
```

**Result set class** that collates individual results:

```java
import com.effacy.jui.rpc.handler.client.IConverter;
import com.effacy.jui.rpc.handler.client.query.ResultSet;

public class MyQueryResultSet extends ResultSet<MyQueryResult> {

    public MyQueryResultSet() {
        super (null, 0);
    }

    public MyQueryResultSet(Iterable<MyQueryResult> results, int totalResults) {
        super (results, totalResults);
    }

    public <S> MyQueryResultSet(Iterable<S> results, IConverter<S, MyQueryResult> converter, int totalResults) {
        super (results, converter, totalResults);
    }
}
```

**Query class** to instigate the result set query:

```java
import com.effacy.jui.rpc.handler.client.query.PageQuery;

public class MyQuery extends PageQuery<MyQueryResultSet> {

    private String keywords;
    
    // For serialisation
    protected MyQuery() {
        super ();
    }

    public MyQuery(int page, int pageSize) {
        super (page, pageSize);
    }

    /**
     * Assign search keywords.
     * 
     * @param keywords
     *                 the keywords.
     * @return this instance.
     */
    public MyQuery keywords(String keywords) {
        setKeywords (keywords);
        return this;
    }

    /**
     * Serialisation.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Serialisation.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    // Add filter criteria.

    @Override
    public boolean filtering() {
        // Anything that would imply a filtering of results. 
        if (!StringSupport.empty (keywords))
            return true;
        return super.filtering ();
    }
}
```

#### Processor classes

**Query processor** processes an incoming `MyQuery` and responding with an instance of `MyQueryResultSet`. Note that `QueryContext` is a suitable context class (defined by the underlying query framework) and you will probably have a separate base class to extend:

```java
import com.effacy.jui.rpc.handler.RPCHandlerProcessor;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

import workforce.app.core.api.BaseLookupProcessor;
import workforce.app.core.api.ProcessorExceptionSupport;
import workforce.app.core.api.QueryNavigationContext;
import workforce.app.core.model.PaginatedQueryResultSet;
import workforce.app.core.model.position.PositionQP;
import workforce.app.core.model.position.PositionQPQuery;
import workforce.app.core.service.logging.Logging;
import workforce.app.remoting.position.PositionQuery;
import workforce.app.remoting.position.PositionQueryResultSet;

@RPCHandlerProcessor
public class MyQueryProcessor extends QueryProcessor<QueryContext, MyQueryResultSet, MyQuery> {

    /**
     * Default constructor.
     */
    public MyQueryProcessor() {
        super (MyQuery.class);
    }

    /**
     * {@inheritDoc}
     *
     * @see workforce.app.core.api.BaseLookupProcessor#process(workforce.app.core.api.QueryNavigationContext,
     *      com.effacy.gwt.rpc.handler.client.RemoteQuery)
     */
    @Logging
    @Override
    protected MyQueryResultSet process(QueryContext context, MyQuery query) throws ProcessorException {
        // Execute query and return results, for example:
        PaginatedQueryResultSet<MyEntity> results = ...;
        return new PositionQueryResultSet (results, MyQueryResultConverter.converter (), (int) results.getTotalResults ());
    }
}
```

**Converter** which is used to convert from the server-side entity (or whatever object captures the data being queried) to the transfer class. This is entirely optional but can be used when transfer classes form composites (it saves a repeating transfer build code):

```java
import com.effacy.jui.rpc.handler.client.IConverter;

public class MyQueryResultConverter {

    public static IConverter<MyEntity, MyQueryResult> converter() {
        return new MyQueryResultConverterImpl ();
    }

    static class MyQueryResultConverterImpl implements IConverter<MyEntity, MyQueryResult> {

        @Override
        public MyQueryResult convert(MyEntity entity) {
            return populate (new MyQueryResult (), entity);
        }

        // This is separated out so that it can be used in an inheritance structure.
        protected MyQueryResult populate(MyQueryResult dto, MyEntity entity) {
            // Populate fields
            return dto;
        }
    }
}

```

#### Client side support

**Store class** used if you want to employ the query in a table or gallery (this is a starter configuration, you may expand on this as needed):

```java
public class MyStore extends PaginatedStore<MyQueryResult> {

    /**
     * The underlying store query (for filtering and pagination).
     */
    private MyQuery query = new MyQuery (0, 10);

    @Override
    protected void requestLoad(int page, int pageSize, ILoadRequestCallback<MyQueryResult> cb) {
        query.setPage (page);
        query.setPageSize (pageSize);
        new MyServiceHandler<MyQueryResultSet> ()
            .onSuccessful (v -> {
                cb.onSuccess (v.getResults (), v.getTotalResults (), query.filtering ());
            }) 
            .onFailure ((v, s) -> {
                if (v.isEmpty ())
                    cb.onFailure ("There was a problem retrieving the results.");
                else
                    cb.onFailure (v.get (0).getMessage ());
            }) 
            .remoteExecute (query);
    }

    @Override
    protected void onClear() {
        query = new MyQuery (0, 10);
    }

    /**
     * Assigns data to a query.
     * 
     * @param updater
     *                to update the passed query.
     */
    public void query(Consumer<MyQuery> updater) {
        if (updater != null)
            updater.accept (query);
        reload (10);
    }

}
```

### Remote load and display

A useful pattern is to remotely load data then display the contents directly into an element.

```java
private Element contentsEl;

new WebApplicationServiceHandler<ItemResultSet>()
    .onSuccessful(v -> {
        buildInto(contentsEl, target -> {
            if (v.getResults().isEmpty()) {
                Div.$ (target).text ("Nothing to do!");
            } else {
                v.forEach(item -> {
                    Div.$ (target).$ (
                        Text.$ (item.getName())
                    );
                });
            }
        });
    })
    .remoteExecute(new ItemQuery(0, 20));
```

## Stores

### Infinite scrolling

Infinite scrolling is a common method of presenting data in tabular or gallery views, allowing a user to continuously scroll through records until reaching the end of the dataset. This differs from the paged model, where users must actively select and navigate between discrete pages of data.

In the context of a data store, infinite scrolling is implemented through a `loadNext` operation. This instructs the store to retrieve the next page of results and append them to the existing tabular or gallery view. The call is triggered when the user scrolls near the bottom of the current view, creating the effect of seamless, continuous scrolling through the dataset.

There are three main challenges associated with implementing this model:

1. **Initial load** - After retrieving the first page of data, the content may not be sufficient to fill the entire view. In such cases, an immediate loadNext() must be triggered to ensure the view is populated.
2. **Scroll detection** - The system must accurately determine when the user is approaching the end of the scrollable view so that the next loadNext() can be triggered at the right moment.
3. **Rendering strategy** - Once new records are retrieved, the application must decide whether to re-render the entire view or to incrementally render only the newly appended records, balancing performance and responsiveness.

Each challenge is addressed below and assumes that there is an element `storeContentEl` that contains the contents of the store (and is what is scrolled).

#### Initial load

When content is loaded into the store a load listener should respond and render the contents into `storeContentEl`. At this point we perform a test to see if the contents fill out the view. If not, then we invoke a `loadNext()` (and continue to do so until all store contents have been loaded or we pass the end of the view).

```java
store.handleAfterLoad(str -> {
    // Build store contents / new records into storeContentEl.
    ...
    // Test if we have filled out the available view.
    if ((store.getTotalAvailable() > store.size()) && (store.getStatus() != IStore.Status.LOADING)) {
        if (storeContentEl.clientHeight < storeContentEl.parentElement.clientHeight)
            store.loadNext();
    }
});
```

#### Scroll detection

The following pattern can be used to detect when one is near the end of the scrollable area. The assumption is the `root` is scrollable (though can be any suitable element that has a constrained or fixed height). Store content is written into this element.

A scroll event handler is attached to the element that monitors its location near the end of the view area and triggers a `loadNext()` on the store when within a suitable threshold. The restriction on loading simply prevent unneccesary loads being invoked.

```java
renderer(root -> {
    root.css("overflow: auto;");
    root.use(n -> storeContentEl = (Element) n);
    root.on((e,n) -> {
        Element el = (Element) n;
        if ((el.scrollHeight > 0) && ((el.scrollHeight - el.clientHeight - el.scrollTop) <= 10)) {
            if ((store.getStatus() != IStore.Status.LOADING))
                store.loadNext();
        }
    }, UIEventType.ONSCROLL);
});
```

#### Rendering strategy

The simplest approach is to re-render the store contents into `storeContentEl`:

```java
buildInto(storeContentEl, r -> {
    store.forEach(item -> {
        // Render item
        ...
    });
});
```

If you want an more of an additive approach we can test the page size against the initial page size (which reflects the advancement).

```java
if (store.getPageSize() > store.getInitialPageSize()) {
    // Amount is what has been added.
    int amount = ((store.getPageSize() - 1) % store.getInitialPageSize()) + 1
    appendInto(galleryEl, r -> {
        store.asList().subList(store.size() - amount, store.size()).forEach(item -> {
            // Render item
            ...
        });
    });
} else {
    buildInto(galleryEl, r -> {
        store.forEach(item -> {
            // Render item
            ...
        });
    });
}
```

The use of `buildInto(...)` will allow for event handlers to be attached (and will dispose of any prior handlers).

## Examples

A number of UI patterns occur with some frequency but are variegated enough in their specificity as to oppose embodiment in a standardised form (i.e. Component); yet being amenable to exemplar (and liable to adaptation).

### Drop selector

```java
...
protected Element selectorFocusEl;
protected Element selectorEl;
protected ActivationHandler selectorHandler;

protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (root -> {
        ...
        Div.$ (cell).style ("selector").by ("selector").$ (profile -> {
            Div.$ (profile).style ("activator").$ (activator -> {
                activator.onclick (e -> onSelectorClick (e));
                A.$ (activator).by ("selectorFocus").$ (content -> {
                    // Label contents.
                });
                Em.$ (activator).style (FontAwesome.caretDown ());
            });
            Div.$ (profile).style ("menu").$ (menu -> {
                // Menu contents.
            });
        });
        ...
    }).build (tree -> {
        selectorFocusEl = tree.first ("selectorFocus");
        selectorEl = tree.first ("selector");
        ...
    });
}

protected void onSelectorClick(UIEvent e) {
    e.stopEvent ();
    if (selectorHandler == null)
        selectorHandler = new ActivationHandler (selectorEl, selectorEl, "open");
    if (selectorHandler.toggle ()) {
        selectorFocusEl.focus ();
        // Anything else that needs to be done on activation.
    }
}
...
```

With corresponding (minimal) CSS:

```css
.selector {
    position: relative;
}

.selector .activator {
    display: flex;
    align-items: center;
    gap: 0.5em;
    cursor: pointer;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 0.5em 1em;
    background: #fff;
}

.selector .menu {
    background: #fff;
	border: 1px solid #ccc;
	border-radius: 3px;
	position: absolute;
	right: 0;
	top: 3.25em;
	display: none;
	z-index: 100000000;
	width: 15em;
}

.selector.open .menu {
    display: block;
    animation: anim .1s ease-in-out;
}
```

### Search panel

This would normally appear in a specific context, such as within the menu of a [drop selector](#drop-selector):

```java
Div.$ (...).$ (panel -> {
    ...
    MyStore store = new MyStore ();
    Div.$ (parent).style ("search").$ (search -> {
        // A search control for entering search keywords. Note the delay
        // handler which prevents invoking a search on every keypress.
        Insert.$ (search, personSearchCtl = Controls.text (cfg -> {
            cfg.style (TextControl.Config.Style.STANDARD)
                .placeholder ("Search ")
                .clearAction ()
                .iconLeft (FontAwesome.magnifyingGlass ())
                .modifiedHandler (DelayedModifiedHandler.create (200, (ctl, v, vp) -> {
                    // Have the store perform a query based on the value in v.
                    // The following is an example assuming the underlying query
                    // has a keywords property.
                    store.query (q -> q.setKeywords (v));
                })
            );
        }));
    });
    Div.$ (panel).style ("results").$ (results -> {
        Insert.$ (results, GalleryCreator.build (
            store,
            GalleryItemCreator.supplier ((r, item) -> {
                // Render content as a card and make clickable.
                Card.$ (item).adorn (FragmentAdornments.padding (Insets.em (1))).onclick (() -> {
                    // Do something when clicked.
                }).$ (card -> {
                    // Example assumes data represents a person so populates card
                    // with an icon and personal deails.
                    Stack.$ (main).horizontal ().align (Align.START).adorn (FragmentAdornments.grow (1)).$ (top -> {
                        Icon.$ (top, FontAwesome.user ()).size (Length.em (2));
                        Stack.$ (top).vertical ().align (Align.START).gap (Length.em (0)).adorn (FragmentAdornments.grow (1)).$ (content -> {
                            H3.$ (content).text (r.getName ());
                            H5.$ (content).text (r.getPosition ());
                        });
                    });
                });
            }),
            cfg -> {
                // Additional gallery configuration.
            }
        ));
    });
});
```

The above internalises the store instance so is suitable for a state component that re-renders.

## Troubleshooting

### I get what looks like an NPE after I make changes to a remoting class

Most likely the deserialiser that is built has not been updated to reflect the changes. Clear the cache in the code server admin screen and re-compile.