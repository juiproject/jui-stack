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
3. [Form patterns](#form-patterns)
4. [State](#state)
5. [JUI components](#jui-components)
6. [Remoting](#remoting)
7. [Examples](#examples)

## Components

### Constructio

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

#### Existing components

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

#### Internalised

The following creates a component that uses **configuration**, uses the **build method** and declares **internalised styles**:

```java
public class MyComponent extends Component<MyComponent.Config> {

    public static class Config extends Component.Config {

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
        return LocalCSS.instance ();
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

    public static MyFrag $(IDomInsertableContainer<?> parent, /* Configuration */) {
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

#### Minimal (with children)

For children we need to go via the create method (though this works fine for the non-child case):

```java
public class MyFrag extends FragmentWithChildren<MyFrag> {

    public static MyFrag $(IDomInsertableContainer<?> parent, /* Configuration */) {
        MyFrag frag = new MyFrag(/* Configuration */);
        if (parent != null)
            parent.insert (frag);
        return frag;
    }

    public MyFrag(/* Configuration */) {
        /* Set properties */
    }

    @Override
    protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
        return /* DOM content */;
    }
    
}
```

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
    public void build(ContainerBuilder<?> parent) {
        /* Build DOM */
    }

}
```

#### Build method (with children)

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
    public void build(ContainerBuilder<?> parent) {
        Div.$ (parent).$ (childcontainer -> {
            /* Build DOM */
            super.build (childcontainer);
        });
    }

}
```

Note that when build you **must not** invoke `build ()` on the DOM builder. Fragments only *contribute* to the build structure, which is built by the calling renderer. If you want access to a node during build then you should use the `apply(...)` method on a build element:

```java
@Override
public void build(ContainerBuilder<?> parent) {
    Div.$ (parent).$ (div -> {
        div.apply (n -> {
            /* Do what you need to with the node */
        });
    });
}
```

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

### Inline modal (with a form)

An inline modal that wraps and processes a form. Note the generic types `Void`; these can be replaced by types as required.

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
            /* Perform action */
            form.success ();
        });
    });
}).open ();
```

### Modal for a pane

```java
public static class MyComponent extends SimpleComponent implements IProcessable<Object> {

    private static IDialogOpener<Void, Object> DIALOG;

    public static void open(Consumer<Optional<Object>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Object, MyComponent>dialog (new MyComponent (), cfg -> {
                cfg.style (ModalStyle.UNIFORM)
                        .title ("Create something")
                        .type (Type.CENTER)
                        .width (Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create something"));
        DIALOG.open (null, cb);
    }

    public MyComponent() {   
        ...
    }
}
```

### In-component modals

There are times when an component wants to open a modal to interrogate and update its own state. An inline modal may do the job but if the interactions within the modal are more complex then a separate component is in order. In these cases the dialog component can be created as a inner class:

```java
public class MyComponent extends SimpleComponent {

    ...

    @Override
    protected void render(Element el) {
        Wrap.$ (el).$ (root -> {
            Btn.$ (btn, "Open").onclick (() -> {
                new MyInnerDialog ().open ();
            });
        }).build ();
    }

    protected void update(/* data */) {
        ...
    }

    /**
     * An inner class that has access to the parent class members.
     */
    class MyInnerDialog extends SimpleComponent {

        /**
         * Wraps this component into a dialog.
         */
        public void open() {
            ModalDialogCreator.build (this, cfg -> {
                cfg.title ("Demo dialog");
                cfg.removeOnClose ();
                cfg.action().label ("close").link ();
                cfg.action ().label ("Apply").handler (ctx -> {
                    apply (); // Can also be invoked by "ctx.contents ().apply ();"
                    ctx.success ();
                });
            }).open ();
        }

        ...

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                ...
            }).build ();
        }

        public void apply() {
            MyComponent.this.update (...);
        }
     }
}

```

## Form patterns

### Create form

The following is a template create form that populates a carrier type `XYZCommand` used to invoke remoting. It implements `IProcessable` to return a `Long` (or whatever the type is for the ID of the newly created entity).

1. The `Void` type stipulates that the form takes no data to populate form fields from (this is used for an update form not a create).
2. The `Long` type is the ID type of the created entity (if not a `Long` then replace accordingly).
3. The `XYZCommand` is some type that the form can populate and that captures what is being changed (i.e. see [Remoting](#remoting)).

```java
public class CreateXYZForm extends ControlForm<Void,XYZCommand> implements IProcessable<Long> {

    /**
     * Construct an instane of the form panel.
     */
    protected CreateXYZForm() {
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
        XYZCommand cmd = apply (
            new XYZCommand (...)
        );

        // Choose the desired remote call here. Assumes that 
        new MyServiceHandler<ResolutionsResult> ()
            .onSuccessful (v -> {
                // The first (and only) element of the resolutions result is
                Notifier.create ().text ("Successfully created").show (2000);
                outcome.accept (v.firstAsLong ());
            })
            .onFailure ((v, s) -> {
                // Failure delivers an empty outcome.
                CreateXYZForm.this.invalidate (v);
                outcome.accept (Optional.empty ());
            })
            .remoteExecute (new ResolutionsResultLookup (), cmd);
        return;
    }

}
```

### Create form as dialog

To turn the [Create form](#create-form) into a dialog:

```java
public class CreateXYZForm extends ControlForm<Void,XYZCommand> implements IProcessable<Long> {

    private static IDialogOpener<Void, Long> DIALOG;

    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Long, CreateXYZForm>dialog (new CreateXYZForm (), cfg -> {
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

Here the dialog makes use of the fact the form implements `IProcessable` meaning that the callback (of type `Consumer<Optional<Long>>`) will be passed through to the `process(...)` method.

### Update form


### Form-in-place

```java
public static class WrapperPanel extends Panel implements INavigationAware {

    private IComponent cpt;

    public WrapperPanel(IComponent cpt) {
        super(new Panel.Config()
            .scrollable()
            .css("background-color: #fff"));
        this.cpt = add (cpt);
    }

    public void onNavigateTo(NavigationContext context) {
        if (cpt instanceof INavigationAware)
            ((INavigationAware) cpt).onNavigateTo(context);
    }
}
```

```java
add (new WrapperPanel (new MyForm ()));
```

## State

### Notifier

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