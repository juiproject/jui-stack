# Styles & themes

*Quick start code and summary information can be found under [Patterns](#patterns).*

CSS styles are used to format the visual presentation of the DOM (for a comprehensive review of CSS refer to [CSS Styles](https://www.w3schools.com/html/html_css.asp)) and can be challenging to organise and maintain effectively. JUI offers three principal means to organisation your CSS:

1. [Global CSS](#global-css) is a single (though possibly split across multiple files) application-level CSS directly included in the *entry-point page*.
2. [Injected CSS](#injected-css) encapsulates one or more CSS files that are injected programmatically (most common) throughout the application.
3. [Localised CSS](#localised-css) where CSS is localised to a component and injected using name-obfuscation (for re-use), also permitting substitution (multiple styles).
4. [Inline styles](#inline-styles) brings styles directly into code.

Each of above is described within. In addition we include the following related topics:

1. [Themes](#themes) describes a mechanism employed by JUI standard components (in the manner in which CSS resources are declared and overridden as well as the employment of CSS variables) to enable re-styling of components.
2. [Fonts and icons](#fonts-and-icons) outlines JUI inclusion (as a convenience) if a mechanism for leveraging the free version of [FontAwesome](https://fontawesome.com/) (which employs CSS as the delivery mechanism).
3. [Override modules](#override-modules) provides a way of overriding stylesheets and JUI source code (for fixes and enhancements) without creating your own JUI distribution.

Finally, as noted at the beginning of this section, [Patterns](#patterns) provides summary guidance and template code that can get you started.

## Global CSS

*Most applications will have a global CSS file (or files) to style the HTML in the entry-point page. This CSS can be extended to declare CSS styles for use within components, however this is not recommended except for the smallest of applications.*

This is probably the easiest approach but one that can be harder to maintain. In this case one creates and references CSS file(s) directly from the HTML page that serves as the entry point to the JUI application. This HTML may look something along the lines of (this examples comes from the playground and is as seen by the browser):

```html
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="/css/ui-global.css" />
    <script src="/com.effacy.jui.playground.TestApplication/com.effacy.jui.playground.TestApplication.nocache.js" />
  </head>
  <body>
    <div id="pageBody"></div>
  </body>
</html>
```

Here a global CSS file `ui-global.css` is provided (this is served up alongside other web assets) and is used by the JUI application to source CSS styles.

?> For the playground this CSS file resides in the `src/main/resources/static/css` directory and is served by Spring MVC.

As you may expect, applying CSS styles is simply a matter of supplying the CSS class by the same name as it appears in the global CSS. The following illustrates this approach when using the `DomBuilder` mechanism (see [Rendering](topic_rendering.md)):

```java
DomBuilder.div (root -> {
    root.style ("my_css_class");
});
```

which creates a single DIV element with the CSS class `my_css_class`:

```css
.my_css_class {
    ...
}
```
Of course, with global CSS, one needs to be careful when nameing and referencing CSS. It is quite easy to end up with styles being applied in unexpected ways (especially when a component includes other components and CSS is scoped).

?> A strategy for keeping styles unique is to use a scoping style per component. For example, a component `MyComponent` could employ a scoping style `cpt_mycomponent` that is applied to the root node. Should a child node may be styled with `active_button` the applicable styling should be declared as `.cpt_mycomponent .active_button { ... }` in the CSS file. Should `active_button` be used in another component it would be suitably scoped as `.cpt_myothercomponent .active_button { ... }`.

## Injected CSS

*This approach permits the use of multiple CSS files which can be focussed on specific sections or components. These CSS files must be injected programmatically and must reside in the applications module package. This is the recommended approach to structuring CSS for components that are not being shared across projects (i.e. non-library components).*

This is a variant on the global case except that the CSS is injected by JUI. The main advatanges are that the CSS can be maintained more closely within the JUI source tree and that changes to the CSS during development are processed immediately through the [code server](app_codeserver.md) and not through the application server (depending on your approach this may otherwise require a restart of the application server). Other than that the principles are identical to the global case.

To see how this works we recall that JUI has the ability to manage static web assets, which it places alongside the JS resources created during [compilation](app_compilation.md). The source for these assets reside under the `public` directory (in the resources source sub-tree) of the module base package.

?> For the **playground** the module base package is `com.effacy.jui.playground` and the `public` directory is physically located at `src/jui/resources/com/effacy/jui/playground/public`. 

It is not sufficient to simply create the CSS file(s) under the `public` directory, you still need to make them available to the DOM. This is done by *injection* whereby JUI will create a dynamic SCRIPT element referencing your CSS file (and automatically resolving the URL path):

```java
static {
    CSSInjector.injectFromModuleBase ("MyComponent.css");
}
```

Here we are injecting the CSS file named `MyComponent.css` (the tacit assumption in the example being that this CSS is only for that specific class, see point (3) below). Note that this can be called multiple times as the `CSSInjector` keeps track of what has already been injected and injects only once.

The following are some strategies to consider when using injected CSS:

1. **Prototyping** When prototyping consider creating a single CSS file that covers the DOM for the prototype. As the protype transitions to re-usable components then start to deconstruct the CSS into sectional or component CSS.
2. **Sectional** Often a section (often representing a page) is created using custom DOM for layout and incorporating any number of components scoped by the section (meaning that they are not used outside the section). Consider creating a CSS file dedicated to the section and injecting from the section component.
3. **Component** As an application evolves component emerge that are (or could be) re-usable. Consider creating CSS for each component and injecting from the component.

As mentioned in [Global CSS](#global-css) an important point to consider is naming of CSS styles and ensuring there are no name clashes. Reiterating the straregy, consider creating a scoping CSS class for a particular concern (i.e. a section or a component) such as `cpt_mycomponent` then appling that style on the root element (of the section or component). From here nest the CSS using the scoping class to maintain style separation.

We close out this section with a final consideration related to timing: when the CSS is actually injected versus when a component using that CSS is rendered. Using the strategy described above (static injection)  injection may be deferred until the class is actually used. This can result in a short delay between rendering and loading of the CSS which can lead to an unsightly malformed layout for a brief period. To alleviate that consider injecting the CSS earlier (such as in the entry point class). The following allows for both approaches using the feature of inhjection being able to be called more than once:

```java
public static void init() {
    CSSInjector.injectFromModuleBase ("MyComponent.css");
}
static {
    init ();
}
```

Then (optionally) in the application entry point class call:

```java
MyComponent.init ();
```

?> If you have encapsulated your components into a separate module (see [Component explorer](ess_create.md#component-explorer)) you can inject these style in a initialiser for the module (that is, a module entry point). This has the additional benefit that when you main application extends `ApplicationEntryPoint` then the main module will not be loaded until all the scripts injected in any of the dependent modules have fully loaded. This avoids some of the timing issues described above. 

In summary injected CSS is a very straight forward approach to handling CSS across sections and components and is the preferred approach when being employed by a application. When components are used across applications (i.e. when building a component library) the name clash problem becomes exhaserbated, or when style variations on a component are desireable, one should consider [localised CSS](#localised-css).

## Localised CSS

The final method is the most robust but is also the most complicated; this is ideally suited to use with re-usable components and has the distinct advantage of name hashing that reduces the likelihood of style pollution (as noted above).

### The CSS interface

To begin we need to declare our CSS styles in an interface, which extends `CSSResource`. Most of the time we will be using this technique for the styles associated with a specific component and in this case we extend `IComponentCSS`. The rest of this documentation will make the assumption that this is the case.

The following is an example styles interface:

```java
public static interface ILocalCSS extends IComponentCSS {

    public String icon_right();

    public String outer();

    public String waiting();

}
```

The name of the interface is not relevant however, by convention, we tend to declare these as inner to the component so give them the standard name `ILocalCSS` (which are scoped to the class name).

?> You may have noticed that we have tended to use underscores within style names rather than dashes (i.e. `my_css_class` as opposed to `my-css-class`) which goes against convention. The reason being that if we were to migrate our global or injected styles to localised one we would need to have corresponding method name in the associated styles interface; dashes are not permitted in this context.

### Creating an instance of the styles

We now make use of (a variant of) the GWT CSS resources mechanism (see to [Css Styling](https://www.gwtproject.org/doc/latest/DevGuideUiCss.html) for details). This employs a feature of the GWT compiler called *rebinding* which can create boiler-plate code, prior to the compilation, using a separate class processor in a fashion similar to [Java Annotation Processing](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Processor.html).

To perform this we need some additional structure and that structure follows (for JUI) a standard pattern (the following is taken from the `com.effacy.jui.ui.client.button.Button` class):

```java
@CssResource({
    IComponentCSS.COMPONENT_CSS,
    "com/effacy/jui/ui/client/button/Button.css",
    "com/effacy/jui/ui/client/button/Button_Override.css"
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
```

We describe the various elements as follows:

1. The class `NormalCSS` (the name, again, does not matter) is abstract and extends `CssDeclaration`. This is the the class that will be extended and implemented during the rebinding process.
2. The `STYLES` static member will hold a singleton instance of our styles implementation. We have to do this otherwise we would get incompatible duplicates (see (3) below).
3. The `styles()` abstract method will be implemented during rebinding and this is where our styles are created. What happens is that the rebinding processor inspects the `@StyleResource` annotation for the sources CSS files to use. These are composed into one big CSS resource with the latter references overriding the earlier ones. The declared CSS classes are matched with those declared on the styles interface (and the match needs to be one-to-one). The names are then hashed (obfuscated) to as to prevent the problem of colliding CSS names (a problem discussed under [Global CSS](#global-css)) and the hashed names are returned by the assciated method in the implemented interface returned by `styles()`.
4. The `instance()` method creates the singleton instance referenced in (2) above. Note the use of `GWT.create(...)` which is where an instance of the rebound class is created.

Another convenient aspect of the above is that we can create multuple variants of the `ILocalCSS` for different stylings. We can do this just by creating another `XXXCSS` class. The following is also taken from `Button`:

```java
@CssResource({
    IComponentCSS.COMPONENT_CSS,
    "com/effacy/jui/ui/client/button/Button.css",
    "com/effacy/jui/ui/client/button/Button_Link.css",
    "com/effacy/jui/ui/client/button/Button_Link_Override.css"
})
public static abstract class LinkCSS implements ILocalCSS {

    private static LinkCSS STYLES;

    public static ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (LinkCSS) GWT.create (LinkCSS.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
    }
}
```

This time we refer to the CSS files `Button_Link.css` and `Button_Link_Override.css` rather than `Button.css` and `Button_Override.css`.  The `Button` component can be supplied which styles to use during construction (there is a little more to it than that as we employ a particular pattern for style changes to components, one that allows more than just changing CSS, but the principle is the same - see `Button.Config.Style`).

### Inlining the stylesheet

You are not restricted to providing file-based stylesheets, you do have the option to inline them. This is quite good for shorter stylesheets or when you are prototyping. To inline you need to declare the styles using the `stylesheet` property of `@CssResource`:

```java
@CssResource(stylesheet = """
    .component {
        position: relative;
        display: inline-flex;
        ...
    } 
    .component .outer {
        margin: 0;
        width: 100%;
        ...
    }
    ...
""")
public static abstract class LinkCSS implements ILocalCSS {

    private static LinkCSS STYLES;

    public static ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (LinkCSS) GWT.create (LinkCSS.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
    }
}
```

You can also combine the inlined stylesheet with file-based ones:

```java
@CssResource(value = IComponentCSS.COMPONENT_CSS, stylesheet = """
    .component {
        position: relative;
        display: inline-flex;
        ...
    } 
    .component .outer {
        margin: 0;
        width: 100%;
        ...
    }
    ...
""")
public static abstract class LinkCSS implements ILocalCSS {

    private static LinkCSS STYLES;

    public static ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (LinkCSS) GWT.create (LinkCSS.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
    }
}
```

The one caveat with inlining is that you cannot override stylesheets (see [Stylesheet overrides](#stylesheet-overrides)). However, this is only a limitation with libraries.

### Using the styles

Now when it comes to using these styles we simply access the style via the static method:

```java
NormalCSS.instance ().styles ().outer ();
```

for example, to apply to an `ElementBuilder`:

```java
el.style (NormalCSS.instance ().styles ().outer ());
```
When it comes to components we override the `Component`'s `styles()` method to supply the styles we want to use (this is not a requirement, this is only if you are using localised styles for a component):

```java
protected ILocalCSS styles() {
    return NormalCSS.instance ().styles ();
}
```

With this in place you can access the CSS classes as follows:

```java
protected INodeProvider buildNode (Element el) {
    return Wrap.$ (el).$ (root -> {
        Div.$ (root).$ (outer -> {
            outer.style (styles ().outer ());
            ...
        });
    }).build ();
}
```

or more compactly:

```java
protected INodeProvider buildNode (Element el) {
    return Wrap.$ (el).$ (root -> {
        Div.$ (root).style (styles ().outer ()).$ (outer -> {
            ...
        });
    }).build ();
}
```

?> As a side note we see that styles used in components extend `IComponentCSS` which declares some standard CSS classes. One of these is `component`. The `Component` will, if styles are supplied by overriding the `styles()` method, apply the `component` CSS class to the root element of the component. Our convention is to scope all CSS with `component` which adds an additional layer of protection against CSS name clashing and pollution.

### Mixing styles (strictness)

You are not required to have every style represented in the styles interface; styles can exist in the CSS file that dont't have a matching method and still can be used by direct reference:

```java
protected INodeProvider buildNode (Element el) {
    return Wrap.$ (el).$ (root -> {
        Div.$ (root).style ("outer").$ (outer -> {
            ...
        });
    }).build ();
}
```

This means you can mix obfuscated styles with non-ofuscated ones. Here you can gain the benefit of scoping (by an obfuscated one) as well a having a localised style sheet while not having to have every style represented in the CSS interface.

This is not really a recommended approach (especially for component libraries) but is certainly a useful feature when converting from a global or injected CSS to a localised one.


## Inline styles

Style inlining is straightforward but comes at the cost of maintanance. However, where highly localised styling, or adjustments need to be made to imposed styling, this can be a convenient option (certainly can be during prototyping).

The `ElementBuilder` (as part of the `DomBuilder` mechanims, see [Rendering](topic_rendering.md)) exposes the `css(...)` family of methods to apply direct styles. Some examples include:

```java
// Using structured styles in the CSS class (not all properties are represnted).
H1.$ (root).css (CSS.MARGIN_TOP, Length.em (0)).css (CSS.PADDING_BOTTOM, Length.px (2));

// Using custom supplied propertied styles.
H1.$ (root).css ("margin-top", "0em").css ("padding-bottom", "2px");

// Using full inlined styles.
H1.$ (root).css ("margin-top: 0em; padding-bottom: 2px;");
```

Note that the structured styles approach is quite good for applying adjustments that are passed through component configuration.

## Themes

Here we describe a number of techniques to make it easy to re-theme a component (specifically a JUI standard component). These consist of:

1. [Global variables and styles](#global-variables-and-styles) are styles and CSS variables that are used across all JUI components.
2. [Isolated changes](#isolated-changes) are small adjustments on a per-component instance case.
2. [Component variables](#component-variables) allows one to override variables using additional styles.
3. [Stylesheet overrides](#stylesheet-overrides) where a project replaces a stylesheet in a module.
4. [Style packs](#style-packs) which allow for components to take on a plurality of styles (including custom styles).

We describe each separately.

### Global variables and styles

*Modifying global variables and styles is suitable for making broad a common theme changes, primarily related to colour palettes. Separate from this, these can be applied to your own components to effect standardisation.*

JUI makes use of a collection of globally declared [CSS variables](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties) to implement a standard (and parameterised) colour and structural model. Auxillary to these are a handful of CSS styles that implement some common behaviours (such a transitions and loading).

These are declared in `Theme.css` and initalised via the `Theme` class (in `com.effacy.jui.ui.client` of the **jui-ui** module and initialised in the modules initialiser entry point). It too follows the override pattern described in [stylesheet override](#stylesheet-overrides) and you can provide your own variable overrides in `com/effacy/jui/ui/client/Theme_Override.css`.

#### Global variables

The global variable mainly declare colour palettes. Broadly speaking the variables declared fall into the following categories:

|Category|Description|
|--------|-----------|
|`--jui-color-primaryXX`|The primary colour palette in shade gradations.|
|`--jui-color-secondaryXX`|The secondary colour palette in shade gradations.|
|`--jui-color-tertiaryXX`|The tertiary colour palette in shade gradations.|
|`--jui-color-neutralXX`|The neutral (gray-like) colour palette in shade gradations.|
|`--jui-color-errorXX`|The error colour palette in shade gradations.|
|`--jui-color-warningXX`|The warning colour palette in shade gradations.|
|`--jui-color-successXX`|The success colour palette in shade gradations.|
|`--jui-color-aux-XX`|Auxilliary colour for common purposes.|
|`--jui-border-radius-XX`|Common radii.|
|`--jui-state--XX`|Common component states.|
|`--jui-line-XX`|Common lines.|
|`--jui-text-XX`|General topography.|
|`--jui-selection-XX`|Common selection.|
|`--jui-ctl-XX`|Common control.|
|`--jui-btn-XX`|Common button.|

Others may be present (see `Theme.css` for details) but there are the main ones.

#### Global styles

A minimal number of globally declared styles and keyframes are employed:

|Style|Description|
|-----|-----------|
|`jui-animation-waiting`| A keyframe that can be used to fade in and out a block that suggested content is not present but being retrieved. An example animation is `animation: jui-animation-waiting 1s ease-in infinite;`. Note that a background colour is expected to be present (i.e. `#f1f1f1`).|
|`jui-animation-fade`| A keyframe that can be used to fade in something (this is used in tabbed panels to fade in page). See also `fade` and the method family `Theme.fade(...)`.|
|`fade`| A CSS class that employs `jui-animation-fade`.|
|`loader`| A CSS class that styles a loading indicator. This should be applied to a single DIV element and sized with the CSS style `font-size`.|

Note that CSS styles can be obtained through `Theme.styles()`.

### Component variables

*Component-level CSS variables can be modified quite easily and afford a mechanism to fine-tune existing JUI components particularly for structural (i.e. size) and colours. The same approach can be used for inheritable styles (such as `font-size`).*

Most of the JUI components make use of [CSS variables](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties) declared on the component root element via the components `.component` CSS class. For example, the `CheckControl` makes use of the following (see `CheckControl.css`):

```css
.component {
    --jui-checkctl-text: var(--jui-ctl-text);
    --jui-checkctl-description: var(--jui-ctl-text);
    --jui-checkctl-labelgap: 0.75em;
}
```

The component specific variables can be overridden in an auxillary CSS class (i.e. one that is global or injected):

```css
.my-checkctl {
    --jui-checkctl-text: green !important;
}
```

Which can then be applied explicitly via the `styles(...)` configuration:

```java
Controls.check(cfg -> {
    cfg.styles("my-checkctl");
    ...
});
```

This is only good for changes that can be parameterised by these variables, but in many cases this is sufficient.

### Isolated changes

If you are looking to make changes on a per-component instance (or fragment) basis then you could consider applying adjustments directly to the root element with the `css(...)` method:

```java
MyComponentCreator.build(cfg -> {
    cfg.css("margin-top: 2em;");
});
```

This is useful for positional changes but is otherwise limited in terms of internal styles. For the latter you can leveage [component variables](#component-variables) (if available):

```java
CheckControlCreator.build(cfg -> {
    cfg.css("--jui-checkctl-text: green;");
}); 
```

which is an alternative to creating a separate (but re-usable) style class.

### Stylesheet overrides

*This is a more aggressive approach to modifying existing styles that go beyond application of inheritable styles at the component root and parameterisation through component variables.*

The idea employed here is that a module can provide replacement code (specifically resources) for that provided my an inherited module. This is one of the easiest approaches to re-styling JUI standard components and involves:

1. Create a `super` sub-directory under the module base and declare `<super-source path="super" />` in the module file. This tells the transpiler to substitute code under this for that in any inherited module.
2. Under `super` create a package structure to the stylesheets you want to override and create suitable versions of those stylesheets.

Having done this and recompiled the JUI code you should see the alternatives being applied. Since this approach is one that is wholesale (i.e. a complete replacement) then all styles need to be included in the revision. This is not always ideal. For this reason the JUI standard components often include a special override version of the main stylesheet and this is included at the end of the declaration list (so takes precedence). Consider the following taken from `Button`:

```java
@CssResource({
    IComponentCSS.COMPONENT_CSS,
    "com/effacy/jui/ui/client/button/Button.css",
    "com/effacy/jui/ui/client/button/Button_Override.css"
})
```

The main stylesheet `Button.css` contains the default styles while `Button_Override.css` is provided but is empty. To adjust the hover colour create `com/effacy/jui/ui/client/button/Button_Override.css` under `super`:

```css
.component .outer:hover {
    background: green !important;
}
```

You should see only the change in the disabled colour.

?>This approach is not limited to CSS files but also works if you want to override source code. This may be necessary if you want to modify JUI classes (i.e. fixing a bug or adding an enhancement) but you do not want to create you own JUI distribution. However, since the source sits outside of the source tree it does not play well with your IDE. A better approach is to create an override module as outlined in [Override modules](#override-modules).

### Style packs

*The most effective approach to re-styling is to create a custom style for a given component. These extend the existing styles and is good when you want to continue to use the existing ones.*

Style packs is a design pattern for the localisation of styles to specific components where more than one variation of a component can appear in a single application. These are generally employed for re-usable components and widely used for JUI standard componenst (an example of which is `Button` which uses styles to style solid, outline and colour variable buttons). 

The general approach is as follows:

1. Create a single `ILocalCSS` style interface as you would for [localised css](#localised-css).
2. Create a single `Style` inside the components `Config` class with a `ILocalCSS styles();` method along with any additional configurable parameters for the style (examples include specific icons or text).
3. Add a `Style` property to the components `Config` class.
4. Override the `protected ILocalCSS styles()` method to return the styles from the config.

An example follows:

```java
public class MyComponent extends Component<Button.Config> {

    public static class Config extends Component.Config {

        public interface Style {

            public ILocalCSS styles();

            // Convenience creator.
            public static Style create(ILocalCSS styles) {
                return new Style () {
                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }
                };
            }

            // Repeat for each style type.
            public static final Style NORMAL = create (NormalCSS.instance ());
        }

        // Initialise with the default style type.
        private Style style = Style.NORMAL;

        ...

        // To set a different style type.
        public Button.Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        ...
    }

    public MyComponent(MyComponent.Config config) {
        super (config);
    }

    ...

    // Return styles from the configuration.
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    // Declares the various styles.
    public static interface ILocalCSS extends IComponentCSS {
        ...
    }

    // Repeat the following for each style type.
    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        ...
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

This example declares a number of standard styles within the class. These can be created outside of the class (for customisation of existing components) along the following lines.

```java
public class MyComponentStyles {

    public static final MyComponent.Style CUSTOM1 = MyComponent.Style.create (Custom1CSS.instance ());

    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        ...
    })
    public static abstract class Custom1CSS implements ILocalCSS {

        private static Custom1CSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (Custom1CSS) GWT.create (Custom1CSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
```

## Fonts and icons

### Font declarations

Sometimes you may want to include a font family into your CSS (see [FontAwesome](#fontawesome) below). The [global CSS](#global-css) mechanism described above allows you to do this using annotations on a CSS interface. The following is take from `FontAwesome`:

```java
...
@CssResource ({ "fontawesome.css", "fa-brands.css", "fa-regular.css", "fa-solid.css" })
@CssResource.Font (name = "Font Awesome 6 Brands", noinline = true, weight = "400", sources = { "fa-brands-400.woff2" })
...
public static abstract class FontCSS implements CssDeclaration {}
...
```

This example creates a global CSS resource with plain declarations for the embodied CSS styles (i.e. not obfuscated so no styles are declared in `FontCSS`). The CSS references a font family of a given weight (this annotation can have repeated use so one may declare sources for other weights of the same family as well as additional families). Each of the `CssResouce.Font` attributes adds to the global CSS declarations of the form:

```css
@font-face {
    font-family:'Font Awesome 6 Brands';
    font-style:normal;
    font-weight:400;
    font-display:block;
    src:url('...') format('woff2');
}
```

The particular usefulness of this is the handling of the font resource URL (which can be a TFF, WOFF or WOFF2 file), which is one of three ways:

1. Allowing the font resource to reside on the classpath (just as with `CssResource`) then packaging as a compilation artefact with the appropriate reference.
2. Inlining the font resource when it is small enough.
3. Allowing the font resource to reside in the `public` directory of the module base package and generating an appropriate reference.

The behaviours are managed by application of annotation attribues:

1. If `useModuleBase` is set to `true` (the default is `false`) the option (3) is employed and the `sources` must be relative references (with no leading slash) to the font resources under the `public` directory.
2. If `useModuleBase` is not set to `true` but `noinline` is set to `true` (the default is false) then the font resources is expected to be found on the classpath and will be inlined into the CSS style (i.e. encoded).
3. If both `useModuleBase` and `noinline` are not set then the font resource will be inlined if sufficiently small others a compilation artefact will be created with a unique name and a reference given to it.

### FontAwesome

[FontAwesome](http://fontawesome.com) is a family of icons delivered as CSS and packaged as fonts. Since it is widely used and there is a free version this version has been packaged into JUI in a manner that makes it convenient to use (the description given in the previous section details how the CSS and fonts are packaged within JUI).

The class `FontAwesome` declares a number of static methods that inject the requisite CSS to present a given icon. Each of these methods can be configured by way of passing any number of additional options. For example the following return the CSS for a image of a spoon that is spinning (as a CSS effect) which can be applied to an element that carries the icon (traditionally an empty I or EM tag):

```java
FontAwesome.spoon (FontAwesome.Option.SPINNING)
```

(as at the time of writing this is `"fas fa-spoon fa-spin"`).

?>The the free version does have some limitations: not all icons are available and for those icons that are available they are not necessarily available in all variations. In general the solid (or `Option.BOLD`) version is given but not all regular (`Option.REGULAR`) versions are supplied. At the time of writing the thin and light are not available.

If you have a license to the **professional version** then you can replace (or suppliment) the font files (i.e the `.woff2` files) using the same `super-source` mechanism described [Overriding global styles](#overriding-global-styles) (this time create a directory structure that mirror `com.effacy.jui.ui.client.icon` and place the resource files within). This will force the compiler to use the extended fonts thereby gaining access to the extra variants.

*There is a **caveat** here in that the `FontAwesome` class only declares static method for icons that are available in some form with the free version. The professional version only fonts won't be represented. In this instance one can emply the `public static String format(String name, Option... options)` method passing the name of the icon as the first argument (i.e. `face-awesome` or `sparkles`).*

## Override modules

Previously we described how you can override (by replacement) CSS files from JUI using [Stylesheet overrides](#stylesheet-overrides) by making use of the `super-source` directive in your module declaration. However, since this source sits outside the source tree your IDE may not play nicely with it. An alternative it to create a overriding module.

To do this create a package `com.effacy.jui` in your JUI source (both source code and resources trees). In the resource tree create the file `JUIContrib.gwt.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//JUI//1.0.0" "jui-module-1.0.0.dtd">
<module>
  <inherits name="com.effacy.jui.ui.UI" />
</module>
```

In the application module where you include the JUI modules also include `JUIContrib`:

```xml
 <inherits name="com.effacy.jui.JUIContrib" />
```

Now simply provide replacements for the stylesheets and source code as needed under the package `com.effacy.jui` (the way the transpilation works, these files will take precedence over those in the JUI modules).

## Patterns

As a convenience template code and a summary of the key points for each of the approaches to stylesheets are described within. You can use this code as a template for your own.

### Global stylesheet

This is the simplest and is sufficiently described in [Global CSS](#global-css). However the following are some useful considerations:

1. You are not bound to a single stylesheet; you can use as many as you like only that you must include each separately.
2. Name clashing is a always a concern and you should take usual care to avoid. There is a plethora of commentary on this topic online.
3. You may want to consider [inline styles](#inline-styles) in conjunction (though somewhat defeats the benefits of CSS styles).

When designing re-usable components you should aim to move to [Localised CSS](#localised-css) being that which best encapsulates the component code and resources and optimises performance.

### Injected stylesheet

The following pattern can be used to establish a style sheet that resides in the modules `public` directory.

```java
static {
    CSSInjector.injectFromModuleBase ("cpt_mycomponent.css");
}

...

// Constructor based renderer.
public MyComponent() {
    renderer (root -> {
        root.style ("cpt_mycomponent");
        ...
    });
}

OR

// Method based renderer.
protected INodeProvider buildNode (Element el) {
    return Wrap.$ (el).$ (root -> {
        root.style ("cpt_mycomponent");
        ...
    }).build ();
}

```

with stylesheet `cpt_mycomponent.css`:

```css
.cpt_mycomponent {
    ...
}

.cpt_mycomponent .mystyle {

}
...
```

Note that:

1. The style sheet must reside in the `public` directory in order for injection to work.
2. The recommended convention is to name the root style `cpt_CPT` where `CPT` is the component name (in lowercase) and the file `cpt_CPT.css`. This helps avoid name clashes (if you choose another naming convention, be sure to take this into consideration).
3. The recommended convention is to apply the root style to the root element and scope all sub-ordinate styles with the root style (again, to help avoid name clashes).

Since injected stylesheets are injected later that local ones you sometimes (not always) may render the component prior to the loading of the CSS. In this case you will see a little flicker as the styles are applied. If this is problematic then you have two options to condsider:

1. Consider using an `init()` method invoked from the application entry point (see discussion at the end of [Injected CSS](#injected-css))
2. Switch to a global stylesheet or to us local styles (see [Migrating from injected to local](#migrating-from-injected-to-local)).

### Local styles

The following pattern can be used in a `SimpleComponent` to seed building out local styles for the component.

```java
@Override
protected ILocalCSS styles() {
    return LocalCSS.instance ();
}

public static interface ILocalCSS extends IComponentCSS {
    // Add relevant styles.
}

@CssResource({  
    IComponentCSS.COMPONENT_CSS,
    "../MyComponent.css"
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
```

with stylesheet `MyComponent.css` located in the code tree (by convention the resources branch of the code tree):

```css
.component {
    ...
}

.component .mystyle {
    ...
}

...
```

Note that:

1. The `GWT` class is the one in `com.google.gwt.core.client`.
2. The `CssResource` reference should include the package path to the CSS file (convention is it name it the same as the class) without a leading slash.
3. Styles can be referenced in code via the `styles()` method (i.e. `styles().mystyle()`).
4. It is safe to inclue CSS styles in the CSS file that are not declared in `ILocalCSS` and these can be used as named (i.e. if there is a class `.mystyle` which is not in `ILocalCSS` then it can be used directly as `mystyle`). This is generally not encouraged as a key benefit of local styles is that they are obfuscated to avoid name clashing.
5. The root style is decalared in `IComponentCSS` as `component` and will be applied automatically by `SimpleComponent` from a call to `styles()`. You should declare this style in your stylesheet and scope other styles by it.
6. The `IComponentCSS.COMPONENT_CSS` is a default stylesheet that declares all styles in `IComponentCSS` so you only need to override the ones you are interested in.

#### Migrating from injected to local

If you want to migrate from injected styles to [local styles](#local-styles) this is relatively stratightforward if you followed the notes above. The steps are:

1. Create the base local styles code (see [below](#local-styles)).
2. In the local stylesheet copy all styles from the injected across and rename the root style to `.component` (this assumes you are following the described convention).
3. For each declared style create an entry in the `ILocalCSS` (i.e. for `mystyle` create `String mystyle();` in `ILocalCSS`).
4. Replace applications of the style with their `ILocalCSS` equivalanet (i.e. for `.style ("mystyle")` replace with `.style (styles ().mystyle ())`).
5. Remove the `CSSInjector` statement and remove the old stylesheet from the `public` directory.