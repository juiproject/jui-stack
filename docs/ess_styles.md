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

## Understanding the styling concerns

*Before choosing a CSS technique it helps to separate the different concerns that arise in a JUI application. These concerns build on one another rather than compete with one another.*

In practice there are three broad styling concerns to keep in mind:

1. **Theming JUI itself** concerns the overall colour and structural language of the application as expressed through JUI's theme tokens.
2. **Styling existing JUI components** concerns adapting standard components to a particular use-case, page, or visual variant.
3. **Styling custom components** concerns the CSS you write for DOM and components that are specific to your application.

These are related, but they are not the same thing, and it is useful to treat them in that order.

### 1. Token-based theming of JUI

JUI standard components are built around a token-based theming model. At the base of that model are theme files that define *reference*, *role*, *scale*, and *component* tokens. Components then consume those tokens and, where appropriate, expose additional `--cpt-*` component variables.

The expectation is that most applications will override the JUI theme to some degree, particularly around colour, accent, surface treatment, spacing emphasis, and component defaults. The standard way to do that is to supply application-owned global CSS, commonly in a file such as `theme.css`, `common.css`, or `jui.css`, and load it at the application level. The setup for that is described under [Global CSS](#global-css) and the theme-specific mechanism is described under [Themes](#themes).

At this level the aim is broad, systemic theming. You are changing the default visual language of existing JUI components through tokens rather than by writing component-specific selectors. This is the lightest-touch and most maintainable form of restyling and should generally be your first step.

### 2. Deeper styling of existing JUI components

Once the global theme is in place there is often a need to adjust particular uses of standard components. In JUI this is commonly done through component variants and component-level variables rather than by immediately reaching for bespoke selectors.

That deeper styling can often be handled by:

1. selecting or defining a component variant;
2. overriding tokens or `--cpt-*` variables for that variant or instance; and
3. only using direct CSS overrides when the component contract does not already expose the control you need.

This is still theming existing JUI components, but at a narrower scope. Rather than redefining the whole system, you are tailoring a button, dialog, table, navigator, or form presentation for a specific context while staying inside the component's intended styling contract.

### 3. Styling custom components and custom DOM

The final concern is styling components or layouts that are specific to your application. This is where [Global CSS](#global-css), [Injected CSS](#injected-css), [Localised CSS](#localised-css), and [Inline styles](#inline-styles) come into play.

Here you are no longer just re-skinning existing JUI components. You are defining the CSS structure for your own sections, fragments, and reusable application components. Even in this case it is recommended that you continue to use the JUI theme tokens where possible, including when assigning inline CSS variables, so that your custom components remain visually aligned with the rest of the application and respond naturally to theme changes.

### How to navigate these concerns

A practical way to approach styling in JUI is:

1. start by establishing the application's global theme and token overrides;
2. use component variants and `--cpt-*` variables to tailor existing JUI components where needed; and
3. introduce application CSS for truly custom components and layouts, while still consuming JUI tokens where possible.

Put another way: use tokens first, component contracts second, and custom selectors third. This keeps the styling model coherent and reduces the amount of CSS that has to know about the internal structure of existing JUI components.


## Global CSS

There are two concerns here: adjusting the existing JUI theme (see [Themes](#themes)) and providing styles for custom components. This section is pertinent to the latter.

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

*This approach permits the use of multiple CSS files which can be focussed on specific sections or components. These CSS files must be injected programmatically and must reside in the applications module package. This is convenient for prototyping but has the drawback that loading occurs on-demand which may result in "flashing" when the HTML is generated prior to the styles being loaded.*

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

The final method is the most robust but is also the most complicated and is ideally suited to use with re-usable components and has the distinct advantage of name hashing that reduces the likelihood of style pollution (as noted above) and a relatively straightforward means of overriding. Having said that, this approach can be "inlined" into a component allowing the style sheet to be declared in the component class as a multi-line string. This provides a nice balance between formality and convenience.

### Structure

Localised CSS consists of the following:

1. A [CSS interface](#css-interface) that declares the styles in use (though this is not a requirement per-se).
2. A [styles instantiator](#creating-an-instance-of-the-styles) which will create an instance of the styles using *rebinding* using stylesheets provided by reference or inlined.
3. [Using the styles](#using-the-styles) by making them available to your component (or fragment).

Generally these are delcared in the component (or fragment) that they pertain to. See [Patterns](#patterns) for examples.

#### CSS interface

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

You will generally declare all styles that you want to use in this interface. However, that is not strictly required. By declaring them you provide a type of strictness for CSS styles and how they are used in your code. Without declaring them you provide styles simply as strings. This is outlined in more detail in [Mixing styles (strictness)](#mixing-styles-strictness).

#### Creating an instance of the styles

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

?>So far described the styles sheets are encoded in separate `css` files. However, with multi-line strings these can be fully inlined into the annotation. This is detailed in [Inlining the stylesheet](#inlining-the-stylesheet).

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

#### Using the styles

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

?>Be wary that when you don't declare the styles explicitly there will be styles that are declared implicitly by way of inheriting from `IComponentCSS` (or any other common style class). For example, `IComponentCSS` declares `component` and `disabled` (among others). These styles **cannot** be referenced directly and have to be resolved via the declared methods (since they will be obfuscated).

This is not really a recommended approach (especially for component libraries) but is certainly a useful feature when converting from a global or injected CSS to a localised one.

### Inlining the stylesheet

*See [Patterns: Local styles (inlined stylesheet)](#local-styles-inlined-stylesheet) for an example.*

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

These are declared in the split theme resources `Theme.Reference.css`, `Theme.Role.css`, `Theme.Scale.css`, `Theme.Component.css`, and `Theme.Legacy.css`, and initialised via the `Theme` class (in `com.effacy.jui.ui.client` of the **jui-ui** module and initialised in the modules initialiser entry point). The final override hook remains `com/effacy/jui/ui/client/Theme_Override.css`.

#### External palettes and application-owned CSS

JUI does **not** require alternate palettes to be registered inside `ThemeCSS`.

The practical model for a JUI application is:

1. Let JUI initialise the base theme through `Theme.init()`.
2. Load your own stylesheet in the application module (for example `common.css`, `theme.css`, or `jui.css`).
3. Define your palette overrides in that stylesheet against the stable body attributes set by `Theme.init()`.
4. Activate the palette by name with `Theme.palette(String)`.

The important detail is that application CSS should **not** target the generated theme class name, because that name comes from a CSS resource and is not a stable selector for external stylesheets. Instead, JUI now marks the body with `data-jui-theme="true"` and uses `data-palette="<name>"` for palette activation.

An application-owned palette can therefore be written as:

```css
body[data-jui-theme][data-palette="knowledgevibe"] {
    --jui-palette-primary-hue: 250;
    --jui-palette-secondary-hue: 210;
    --jui-palette-chroma-peak: 0.11;
}
```

and activated with:

```java
Theme.palette ("knowledgevibe");
```

If you want to return to the default palette, call:

```java
Theme.palette ((String) null);
```

The enum-based `Theme.Palette` remains a convenience for built-in JUI palettes, but application code should generally prefer `Theme.palette(String)` for palettes defined in external CSS.

#### Global variables

The global variables declare colour palettes. The palettes are **derived from a small set of axes** — typically 7 hue values plus 4 chroma values — via `oklch()` + `calc()` in `Theme.Reference.css`. Every `-XX` step across every family targets the same OKLCH lightness (ladder: 05=0.97, 10=0.94, 20=0.88, 30=0.80, 40=0.70, 50=0.58, 60=0.48, 70=0.38, 80=0.28, 90=0.20). Alternate palettes override only the axes. See the package README in `jui-ui/src/main/java/com/effacy/jui/ui/client/README.md` for the full axis list and authoring guide.

|Category|Description|
|--------|-----------|
|`--jui-palette-*-hue`, `--jui-palette-*-chroma-peak`|Palette axes — the authoring surface for a palette. Changing these re-derives the full `--jui-color-*` ramp.|
|`--jui-color-primaryXX`|The primary (brand) colour family, derived from `--jui-palette-primary-hue` and `--jui-palette-chroma-peak`. Teal by default.|
|`--jui-color-secondaryXX`|The secondary brand colour family. Teal-green by default.|
|`--jui-color-inkXX`|The ink (cool neutral) family, derived from `--jui-palette-ink-*`. Used for text and chrome. Formerly named `tertiary`.|
|`--jui-color-neutralXX`|The neutral family, derived from `--jui-palette-neutral-*`. Pure gray by default; palettes may tint it but doing so affects every surface.|
|`--jui-color-errorXX`|Error colour family. Uses its own `--jui-palette-error-chroma-peak` so it stays alarming in muted palettes.|
|`--jui-color-warningXX`|Warning (amber) family.|
|`--jui-color-successXX`|Success family.|
|`--jui-color-infoXX`|Info (clear blue) family. Absorbs the former `aux-blue` and `aux-focus1/2` tokens.|
|`--jui-color-aux-white` / `--jui-color-aux-black`|Absolute white and black. Palettes must not override these.|
|`--jui-border-radius-XX`|Common radii.|
|`--jui-state--XX`|Common component states.|
|`--jui-line-XX`|Common lines.|
|`--jui-text-XX`|General topography.|
|`--jui-selection-XX`|Common selection.|
|`--jui-ctl-XX`|Common control.|
|`--jui-btn-XX`|Common button.|

Others may be present (see the `Theme.*.css` resources for details) but these are the main ones.

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
@CssResource({  
    IComponentCSS.COMPONENT_CSS,
    ...
})
public abstract class MyComponentStyles implements MyComponent.ILocalCSS {

    public static final MyComponent.Style CUSTOM1 = MyComponent.Style.create (MyComponentStyles.instance ());

    private static MyComponent.ILocalCSS STYLES;

    public static SelectionGroupControl.ILocalCSS instance() {
        if (STYLES == null) {
            STYLES = (MyComponentStyles) GWT.create (MyComponentStyles.class);
            STYLES.ensureInjected ();
        }
        return STYLES;
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
public class MyComponent extends SimpleComponent {

    ...

    /************************************************************************
     * CSS styles
     ************************************************************************/

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

### Local styles (inlined stylesheet)

For a version of the above that uses an inlined stylesheet:

```java
public class MyComponent extends SimpleComponent {

    ...

    /************************************************************************
     * CSS styles
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    public static interface ILocalCSS extends IComponentCSS {
        // Add relevant styles.
    }

    @CssResource(value = IComponentCSS.COMPONENT_CSS, stylesheet = """
        .component {
            ...
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

This includes the default `IComponentCSS.COMPONENT_CSS` styles (declaration only, so you don't need to worry about them).
### Design-system token tiers

The current theme is structured as four tiers that should be overridden in order of intent:

1. `reference` tokens such as `--jui-color-*` provide raw palette values.
2. `role` tokens such as `--jui-role-*` provide semantic colours and states.
3. `scale` tokens such as `--jui-space-*`, `--jui-font-*`, and `--jui-radius-*` provide shared rhythm.
4. `component` tokens such as `--jui-comp-*` provide family-level defaults.

The preferred public component tuning layer is now `--cpt-*`. Component styles should consume `--cpt-*`, with those values sourcing from `--jui-comp-*`, `--jui-role-*`, and `--jui-scale-*` tokens. Downstream applications should prefer `--jui-role-*`, then `--jui-comp-*`, then `--cpt-*`, and use stylesheet overrides only when a structural exception is needed.

See also:

- [CSS component contracts](css_component_contracts.md)
- [CSS upgrade notes](css_upgrade_2026_04.md)

# Appendix

## Token reference

This appendix is the companion reference for the design-system tokens introduced in [the *Design-system token tiers* note above](#design-system-token-tiers). It covers three things in order:

1. the **tier structure** — which layer does what, and which tier you should reach for when overriding;
2. **creating a palette** — the axes-first authoring model and the Palette Studio tool;
3. a **token catalogue** — every token JUI ships, grouped by tier, with how each one should be used.

### Tier structure

The theme is organised as a cascade. Later tiers consume earlier tiers; consumers should usually override at the highest tier that still carries their intent.

```
reference  →  role  →  scale  →  component  →  --cpt-*  →  component selectors
(palette)     (semantic)   (rhythm)    (family defaults)   (per-component knobs)
```

Each tier lives in its own CSS file, injected in this order:

| # | File | Tier | Purpose |
|---|---|---|---|
| 1 | `Theme.Reference.css` | **reference (+ palette axes)** | The palette itself — axes and the 80+ `--jui-color-*` ramp tokens derived from them. |
| 2 | `Theme.Reference.<Name>.css` | **reference** | Alternate palettes (e.g. `Theme.Reference.Editorial.css`). Overrides only the axes it wants. |
| 3 | `Theme.Role.css` | **role** | Semantic names independent of palette. `--jui-role-surface-*`, `--jui-role-text-*`, `--jui-role-interactive-*`, etc. |
| 4 | `Theme.Scale.css` | **scale** | Shared rhythm: spacing, type, radius, elevation, motion. |
| 5 | `Theme.Component.css` | **component** | Family-level defaults for every component group. `--jui-comp-button-*`, `--jui-comp-dialog-*`, etc. |
| 6 | `Theme.Legacy.css` | **legacy** | Compatibility shims for retired token names (`--jui-text`, `--jui-btn-*`, `--jui-color-tertiary*`, `--jui-color-aux-focus1/2`, `--jui-color-aux-blue`). Not a design surface. |
| 7 | `Theme_Override.css` | **override** | Final hook for project-level last-mile tweaks. |

And within each component's own stylesheet there is a seventh layer:

- **`--cpt-*`** — declared at the component's root selector (typically `.component`), sourced from `--jui-comp-*` and scale tokens. This is the preferred *public* tuning surface for an individual component or variant.

#### Which tier should I override?

Pick the tier that matches the *intent* of the change. Going finer than you need creates more work and more drift; going coarser than you need spreads the change further than intended.

| I want to … | Override at |
|---|---|
| Retint the brand hue across the app | `--jui-palette-*-hue` axis (reference) |
| Change dark/light surface hierarchy | `--jui-role-surface-*` |
| Swap a single component's link colour | `--cpt-*` on that component |
| Tune all buttons consistently | `--jui-comp-button-*` |
| Widen base spacing everywhere | `--jui-space-*` (scale) |
| Change focus ring colour | `--jui-role-focus-ring` |
| A one-off structural exception | stylesheet override in `Theme_Override.css` |

The default rule of thumb:

> Prefer **role** → **component** → **`--cpt-*`** → stylesheet. Reach for stylesheet overrides only when no token can carry the change.

### Creating a palette

A palette is not a set of 80+ hand-picked colours. It is a compact set of **axes** — hues and chroma peaks — from which JUI derives the full ramp via `oklch()` + `calc()`. The derivations live in `Theme.Reference.css`; a new palette only overrides the axes.

Two contracts apply to every palette:

- **Lightness ladder** — fixed for every family: `-05` L=0.97, `-10` L=0.94, `-20` L=0.88, `-30` L=0.80, `-40` L=0.70, `-50` L=0.58, `-60` L=0.48, `-70` L=0.38, `-80` L=0.28, `-90` L=0.20.
- **Chroma curve** — a bell around `-50`, ratios of the family's chroma-peak: `-05` 0.12, `-10` 0.27, `-20` 0.46, `-30` 0.65, `-40` 0.85, `-50` 1.00, `-60` 0.96, `-70` 0.81, `-80` 0.58, `-90` 0.38.

Neutral is the exception — its chroma is constant across steps.

#### Palette Studio

The easiest way to build a palette is the **[Palette Studio](tools/palette-studio.html)** tool that lets you:

- Paste a brand palette as hex values. The studio extracts the hue of each and re-targets to JUI's shared ladder.
- Tune chroma-peak sliders to hit muted / balanced / vivid characters.
- Tint the neutral stack subtly (see "Neutrals should carry a trace of the palette's temperature", below).
- Tint the page canvas without disturbing bounded surfaces.
- Live-preview the effect on mock JUI components — buttons, inputs, tabs, toggles, a dialog, cards.
- Copy a complete `.theme[data-palette="..."]` block, with both axis values and resolved ramps, ready to save as `Theme.Reference.<Name>.css`.

See also the "Adding an alternate palette" section of `jui-ui/src/main/java/com/effacy/jui/ui/client/README.md` for the hand-authoring path and the structural rules (registering in `Theme.java`, adding to the `Theme.Palette` enum, runtime switching via `Theme.palette(...)`).

#### Key palette rules

- **Keep hue constant across a family** — don't drift the hue between light and dark steps.
- **Pick hues at least 25–30° apart** from other families so they stay visually distinct at low chroma.
- **Tune neutral chroma to match the palette's temperature.** Pure-gray neutrals (`chroma: 0`) against warm Ink text or Bone canvas will clash. A small `--jui-palette-neutral-chroma` (0.005–0.015) at the palette's hue harmonises surfaces. Keep neutral chroma well below any dedicated page-tint colour.
- **Page canvas is an optional override** (`--jui-role-surface-canvas`). Bounded areas — dialogs, panels, control fields — should stay white-anchored; only the outer canvas needs tinting for an editorial feel.

### Token catalogue

Every token JUI ships. Grouped by tier. Descriptions explain *how the token should be used*, not just what it is.

#### Palette axes (reference-tier authoring surface)

Declared in `Theme.Reference.css`. These are the authoring knobs for a palette — change them (or override them in an alternate palette) and the full `--jui-color-*` ramp re-derives.

| Token | Default | Purpose |
|---|---|---|
| `--jui-palette-primary-hue` | 210 | OKLCH hue (0–360°) for the `primary` family. |
| `--jui-palette-secondary-hue` | 170 | Hue for the `secondary` family. |
| `--jui-palette-ink-hue` | 255 | Hue for the `ink` (text/chrome) family. |
| `--jui-palette-neutral-hue` | 0 | Hue for the `neutral` family. Only matters when neutral chroma > 0. |
| `--jui-palette-error-hue` | 25 | Hue for the `error` family. |
| `--jui-palette-warning-hue` | 65 | Hue for the `warning` family. |
| `--jui-palette-success-hue` | 145 | Hue for the `success` family. |
| `--jui-palette-info-hue` | 240 | Hue for the `info` family. |
| `--jui-palette-chroma-peak` | 0.15 | Chroma at step -50 for primary, secondary, warning, success, info. Drop to 0.03–0.05 for muted palettes. |
| `--jui-palette-ink-chroma-peak` | 0.035 | Chroma at step -50 for ink. Always low. |
| `--jui-palette-error-chroma-peak` | 0.18 | Chroma at step -50 for error. Kept separate so destructive reads alarming in muted palettes. |
| `--jui-palette-neutral-chroma` | 0 | Constant chroma across all neutral steps. `0` = pure gray. Raise to 0.005–0.015 to harmonise neutrals with a warm/cool palette temperature. |

#### Reference tokens (colour ramps)

Declared in `Theme.Reference.css`. Derived from the axes above. Ten steps per family (`-05` lightest through `-90` darkest). Component CSS should not normally consume these directly — prefer role tokens.

| Family | Tokens | Purpose |
|---|---|---|
| `primary` | `--jui-color-primary05` through `-primary90` | Brand accent. Step `-50` is the canonical "brand colour"; step `-70` is a strong variant. |
| `secondary` | `--jui-color-secondary05` through `-secondary90` | Second brand accent. Use when a UI needs two distinct brand tones (e.g. two-toned visualisations, header + accent). |
| `ink` | `--jui-color-ink05` through `-ink90` | Text and chrome family — low-chroma but *not* pure gray so text reads as slightly warmer or cooler than neutrals. Formerly `tertiary`. |
| `neutral` | `--jui-color-neutral05` through `-neutral90` | Surface backgrounds, borders, dividers, disabled states. Tinted per palette via `--jui-palette-neutral-chroma`; pure gray by default. |
| `error` | `--jui-color-error05` through `-error90` | Destructive / error feedback. High chroma even in muted palettes. |
| `warning` | `--jui-color-warning05` through `-warning90` | Caution / warning feedback. |
| `success` | `--jui-color-success05` through `-success90` | Positive / success feedback. |
| `info` | `--jui-color-info05` through `-info90` | Informational feedback; also used for focus rings (`info40`/`info20`). |
| `aux` | `--jui-color-aux-white`, `--jui-color-aux-black` | Absolute white and black. Palettes must not override these. Use `-aux-white` for text on dark accents (guaranteed contrast) and `-aux-black` only where a true black is needed. |

#### Role tokens (semantic)

Declared in `Theme.Role.css`. Describe intent independent of palette. This is the preferred layer for consumer overrides: restyle the app by changing roles, not by overriding the palette.

**Surfaces** — the painted backgrounds of containers.

| Token | Purpose |
|---|---|
| `--jui-role-surface-canvas` | Outermost page background. Usually pure white (`aux-white`), but palettes may tint it (e.g. Bone). |
| `--jui-role-surface-overlay` | Floating-above-the-page surfaces — dialogs, popovers, toasts. Usually pure white. |
| `--jui-role-surface-raised` | Standard content surfaces — cards, panels, control fields. Slightly lower-lightness than overlay so controls feel sunken into paper. |
| `--jui-role-surface-muted` | Chrome surfaces — dialog header/footer, tab bars, table headings. Pale and clearly chrome-like. |
| `--jui-role-surface-sunken` | Disabled control backgrounds, tab-hover states. Noticeably darker than muted. |
| `--jui-role-surface-accent` | Accent-tinted surface for hero cards, featured sections. Pale tint of the primary hue. |
| `--jui-role-surface-info` | Background for info notifications. Pale info tint. |
| `--jui-role-surface-success` | Background for success notifications. Pale success tint. |
| `--jui-role-surface-warning` | Background for warning notifications. Pale warning tint. |
| `--jui-role-surface-error` | Background for error notifications and error-state surfaces. Pale error tint. |

**Borders** — 1-px stroke colours for dividers and edges.

| Token | Purpose |
|---|---|
| `--jui-role-border-subtle` | Row separators, dividers between related items. Barely visible. |
| `--jui-role-border-default` | Default component edge — control fields, dialog outlines, card borders. |
| `--jui-role-border-strong` | Emphasised edges — active tab underline, selected item border. |
| `--jui-role-border-contrast` | High-contrast border or text-helper colour, used when a darker label-like shade is needed on a muted surface. |

**Text** — foreground colours.

| Token | Purpose |
|---|---|
| `--jui-role-text-default` | Body text and primary reading colour. |
| `--jui-role-text-muted` | Secondary text: captions, helper text, placeholders. |
| `--jui-role-text-heading` | Headings h1–h6. Often same as `text-default` but palettes may diverge. |
| `--jui-role-text-heading-subtle` | Subheadings / kickers. Slightly softer than headings. |
| `--jui-role-text-disabled` | Disabled labels and read-only text. |
| `--jui-role-text-inverse` | Text on dark or accent surfaces (e.g. filled primary button label). |
| `--jui-role-text-link` | Link text. |
| `--jui-role-text-link-hover` | Link hover state. |

**Interactive** — the colour signature of actionable elements.

| Token | Purpose |
|---|---|
| `--jui-role-interactive-primary` | Filled primary button / active state accent. |
| `--jui-role-interactive-primary-hover` | Hover state of primary. |
| `--jui-role-interactive-primary-strong` | Stronger variant — pressed state, emphasis. |
| `--jui-role-interactive-primary-on` | Text/icon colour drawn *on top of* a primary-filled surface. |
| `--jui-role-interactive-secondary` | Secondary brand accent equivalent. |
| `--jui-role-interactive-secondary-hover` | Hover of secondary. |

**Feedback** — semantic status colours.

| Token | Purpose |
|---|---|
| `--jui-role-feedback-info` | Info accent (dots, badges, strokes). |
| `--jui-role-feedback-success` | Success accent. |
| `--jui-role-feedback-warning` | Warning accent. |
| `--jui-role-feedback-error` | Error / destructive accent. |

**State** — focus, disabled, and selection.

| Token | Purpose |
|---|---|
| `--jui-role-focus-ring` | Keyboard-focus outline colour. Usually derived from the info family. |
| `--jui-role-focus-shadow` | Outer glow of the focus ring. |
| `--jui-role-disabled` | Disabled element fill / muted foreground marker. |
| `--jui-role-disabled-strong` | Disabled element stroke or deeper disabled state. |
| `--jui-role-selection` | Selected text background / selected row accent. |
| `--jui-role-selection-muted` | Softer selection indicator — hover-to-select previews. |
| `--jui-role-selection-active` | Active selection during a gesture (e.g. drag). |

#### Scale tokens (rhythm)

Declared in `Theme.Scale.css`. Shared across all components so layouts stay rhythmic without each component inventing its own units.

**Spacing** — all padding / gap / margin values should pick from this scale.

| Token | Value | Rough use |
|---|---|---|
| `--jui-space-0` | 0 | No space. |
| `--jui-space-1` | 0.25rem | Tight gap (e.g. icon + label). |
| `--jui-space-2` | 0.5rem | Small gap inside a control. |
| `--jui-space-3` | 0.75rem | Between tight siblings (label + input). |
| `--jui-space-4` | 1rem | Default block padding. |
| `--jui-space-5` | 1.25rem | Comfortable block padding. |
| `--jui-space-6` | 1.5rem | Between sibling blocks (rows in a form). |
| `--jui-space-8` | 2rem | Section-level gaps. |
| `--jui-space-10` | 2.5rem | Large separator. |
| `--jui-space-12` | 3rem | Group-level indent / deep separation. |

**Typography** — family, size, weight, line height.

| Token | Value / example | Purpose |
|---|---|---|
| `--jui-font-family-sans` | Arial, sans-serif | Default UI font. |
| `--jui-font-family-mono` | ui-monospace, monospace | Code / monospace contexts. |
| `--jui-font-size-xs` | 0.75rem | Captions, micro-labels. |
| `--jui-font-size-sm` | 0.875rem | Secondary text. |
| `--jui-font-size-md` | 1rem | Body default. |
| `--jui-font-size-lg` | 1.125rem | Lead paragraphs, emphasised body. |
| `--jui-font-size-xl` | 1.25rem | Subheadings. |
| `--jui-font-size-2xl` | 1.5rem | Section headings / dialog titles. |
| `--jui-font-weight-regular` | 400 | Default body weight. |
| `--jui-font-weight-medium` | 500 | Buttons, labels, mild emphasis. |
| `--jui-font-weight-semibold` | 600 | Headings, strong labels. |
| `--jui-font-weight-bold` | 700 | Strong emphasis. |
| `--jui-line-height-tight` | 1.2 | Headings, short labels. |
| `--jui-line-height-normal` | 1.5 | Body paragraphs. |

**Radius** — corner rounding.

| Token | Value | Use |
|---|---|---|
| `--jui-radius-none` | 0 | Hard edges. |
| `--jui-radius-xs` | 2px | Almost-square controls. |
| `--jui-radius-sm` | 4px | Standard control radius (inputs, small buttons). |
| `--jui-radius-md` | 6px | Cards, panels. |
| `--jui-radius-lg` | 10px | Dialogs, popovers. |
| `--jui-radius-pill` | 999px | Fully-rounded pills (segmented controls, badges). |

**Control height** — base height for form controls and buttons.

| Token | Value | Use |
|---|---|---|
| `--jui-control-height` | 2.35em | Keeps all control-like components (`input`, `button`, `select`) at a consistent baseline height. |

**Elevation** — box-shadow presets for floating surfaces.

| Token | Purpose |
|---|---|
| `--jui-elevation-0` | `none`. Flat. |
| `--jui-elevation-1` | Subtle raise — cards on canvas, hover lifts. |
| `--jui-elevation-2` | Dialogs, popovers. |
| `--jui-elevation-3` | Top-level overlays (toasts, dropped-on-everything menus). |

**Motion** — shared animation durations and easings.

| Token | Value | Use |
|---|---|---|
| `--jui-duration-fast` | 150ms | Hover states, small state transitions. |
| `--jui-duration-standard` | 240ms | Open/close of disclosures, tab transitions. |
| `--jui-duration-slow` | 400ms | Page-scale transitions, route changes. |
| `--jui-ease-standard` | cubic-bezier(0.2, 0, 0, 1) | Default entrance/exit curve. |
| `--jui-ease-emphasis` | cubic-bezier(0.4, 0, 0.1, 1) | Sharper emphasis curve for attention-drawing motion. |

#### Component-family tokens (`--jui-comp-*`)

Declared in `Theme.Component.css`. Family-level defaults sourced from role and scale tokens. Use when you want to retune a component family consistently (all buttons, all controls) without editing every component stylesheet. Component stylesheets typically expose `--cpt-*` tokens that source from these.

**Control** — shared defaults for form controls.

| Token | Purpose |
|---|---|
| `--jui-comp-control-font-family` | Font family for control text. |
| `--jui-comp-control-height` | Control height baseline. |
| `--jui-comp-control-surface` | Control field background (enabled). |
| `--jui-comp-control-surface-disabled` | Control field background (disabled). |
| `--jui-comp-control-surface-readonly` | Control field background (read-only). |
| `--jui-comp-control-surface-offset` | Inner offset / alternative surface (e.g. inline trailing-icon backdrop). |
| `--jui-comp-control-surface-waiting` | Waiting/loading placeholder surface. |
| `--jui-comp-control-border` | Default control border colour. |
| `--jui-comp-control-radius` | Corner radius. |
| `--jui-comp-control-text` | Entered text colour. |
| `--jui-comp-control-text-placeholder` | Placeholder text colour. |
| `--jui-comp-control-text-disabled` | Disabled text colour. |
| `--jui-comp-control-text-readonly` | Read-only text colour. |
| `--jui-comp-control-text-header` | Heading-adjacent label colour. |
| `--jui-comp-control-text-subtle` | Secondary label / hint text. |
| `--jui-comp-control-text-link` | Inline link colour inside a control area. |
| `--jui-comp-control-text-offset` | Offset label colour for compound controls. |
| `--jui-comp-control-action` | Trailing-action icon / toggle colour. |
| `--jui-comp-control-action-disabled` | Trailing action when disabled. |
| `--jui-comp-control-action-readonly` | Trailing action when read-only. |
| `--jui-comp-control-action-hover` | Trailing action hover. |
| `--jui-comp-control-active` | Active-state accent (selected toggle, radio fill). |
| `--jui-comp-control-active-bg` | Active-state background tint. |
| `--jui-comp-control-focus` | Focus ring colour. |
| `--jui-comp-control-focus-offset` | Outer focus glow. |
| `--jui-comp-control-error-focus` | Focus ring colour in error state. |
| `--jui-comp-control-error-focus-offset` | Outer glow in error-focus state. |
| `--jui-comp-control-opacity-disabled` | Opacity used for disabled appearance. |
| `--jui-comp-control-opacity-readonly` | Opacity used for read-only appearance. |

**Button** — shared defaults for buttons. Variant-specific surfaces follow the `-danger`, `-warning`, `-success`, `-outline`, `-link` naming convention.

| Token | Purpose |
|---|---|
| `--jui-comp-button-height` | Button height baseline. |
| `--jui-comp-button-surface` | Primary button fill. |
| `--jui-comp-button-surface-hover` | Primary button hover. |
| `--jui-comp-button-surface-disabled` | Button fill when disabled. |
| `--jui-comp-button-border` | Default button border. |
| `--jui-comp-button-border-disabled` | Button border when disabled. |
| `--jui-comp-button-radius` | Corner radius. |
| `--jui-comp-button-text` | Label colour on a filled button. |
| `--jui-comp-button-text-disabled` | Disabled label colour. |
| `--jui-comp-button-waiting-text` | Waiting/loading label colour. |
| `--jui-comp-button-focus-border` | Focus ring colour. |
| `--jui-comp-button-focus-shadow` | Outer focus glow. |
| `--jui-comp-button-danger-surface` / `-hover` / `-border` | Danger (destructive) variant. |
| `--jui-comp-button-warning-surface` / `-hover` / `-border` | Warning variant. |
| `--jui-comp-button-success-surface` / `-hover` / `-border` | Success variant. |
| `--jui-comp-button-outline-surface` / `-hover` / `-text` / `-border` | Outlined variant. |
| `--jui-comp-button-link-text` / `-hover` | Link-styled button. |

**Tabset** — tab bars.

| Token | Purpose |
|---|---|
| `--jui-comp-tabset-surface` | Tab bar background. |
| `--jui-comp-tabset-surface-alt` | Alternate surface (e.g. active tab contrast background). |
| `--jui-comp-tabset-surface-hover` | Tab hover background. |
| `--jui-comp-tabset-surface-active` | Active tab fill / bottom-border colour. |
| `--jui-comp-tabset-border` | Tabset boundary. |
| `--jui-comp-tabset-text` | Tab label. |
| `--jui-comp-tabset-text-muted` | Inactive tab label colour. |
| `--jui-comp-tabset-text-active` | Active tab label (on contrast background). |
| `--jui-comp-tabset-warn` | Warning indicator colour on a tab. |
| `--jui-comp-tabset-shadow` | Drop shadow below selected tab. |

**Container** — panels, sections, groups.

| Token | Purpose |
|---|---|
| `--jui-comp-container-surface` | Panel / section background. |
| `--jui-comp-container-surface-offset` | Subtly-differentiated inner region (e.g. aside within a panel). |

**Dialog** — modal dialogs.

| Token | Purpose |
|---|---|
| `--jui-comp-dialog-surface` | Dialog body background. |
| `--jui-comp-dialog-border` | Dialog outline. |
| `--jui-comp-dialog-radius` | Dialog corner radius. |
| `--jui-comp-dialog-shadow` | Dialog drop shadow (typically `--jui-elevation-2`). |
| `--jui-comp-dialog-chrome` | Header/footer chrome fill. |
| `--jui-comp-dialog-header-surface` | Dialog header background. Use this in application `theme.css` when you want a default header surface for all dialogs. |
| `--jui-comp-dialog-header-divider` | Dialog header bottom divider. |
| `--jui-comp-dialog-footer-surface` | Dialog footer background. Use this in application `theme.css` when you want a default footer surface for all dialogs. |
| `--jui-comp-dialog-footer-divider` | Dialog footer top divider. |
| `--jui-comp-dialog-heading` | Dialog title text. |
| `--jui-comp-dialog-subheading` | Subtitle / secondary header text. |
| `--jui-comp-dialog-icon` | Header icon colour. |
| `--jui-comp-dialog-link` | Inline link colour inside a dialog. |
| `--jui-comp-dialog-close` | Close-button colour. |

**Table** — data tables.

| Token | Purpose |
|---|---|
| `--jui-comp-table-header-surface` | Header row background. |
| `--jui-comp-table-border` | Outer table border. |
| `--jui-comp-table-row-border` | Between-row divider. |
| `--jui-comp-table-heading-text` | Header cell text. |
| `--jui-comp-table-heading-icon` | Sort / helper icon colour. |

**Notification** — info/success/warning/error banners and toasts.

| Token | Purpose |
|---|---|
| `--jui-comp-notification-radius` | Corner radius. |
| `--jui-comp-notification-info-accent` / `-border` / `-surface` | Info variant accent, left-edge border, fill. |
| `--jui-comp-notification-success-accent` / `-border` / `-surface` | Success variant. |
| `--jui-comp-notification-error-accent` / `-border` / `-surface` | Error variant. |

**Form** — standard form layout and affordances.

| Token | Purpose |
|---|---|
| `--jui-comp-form-header` | Form heading text. |
| `--jui-comp-form-instruction` | Instruction text below heading. |
| `--jui-comp-form-footer` | Footer / guidance text. |
| `--jui-comp-form-text` | Default form text. |
| `--jui-comp-form-text-error` | Error-message text. |
| `--jui-comp-form-text-disabled` | Disabled text. |
| `--jui-comp-form-help-surface` / `-text` / `-radius` | Help tooltip background, text, radius. |
| `--jui-comp-form-error-surface` / `-icon` / `-text` / `-radius` | Block-level error indicator. |
| `--jui-comp-form-separator` | Divider between form groups. |

#### Component-local `--cpt-*` tokens

Declared inside individual component stylesheets, at the component's root selector. They source from `--jui-comp-*`, role tokens, and scale tokens. Consumers should prefer overriding `--cpt-*` for component-specific tweaks.

Common prefixes include `--cpt-btn-*`, `--cpt-textctl-*`, `--cpt-checkctl-*`, `--cpt-form-*`, `--cpt-modaldialog-*`, `--cpt-table-*`, `--cpt-notification-*`. The complete list is documented per-component in each component's stylesheet and in [css_component_contracts.md](css_component_contracts.md).

#### Legacy tokens

Declared in `Theme.Legacy.css`. Aliases for retired tokens, kept so older downstream code keeps working.

| Prefix | Replaced by |
|---|---|
| `--jui-text`, `--jui-text-*` | `--jui-role-text-*` |
| `--jui-line`, `--jui-line-*` | `--jui-role-border-*` |
| `--jui-selection`, `--jui-selection-*` | `--jui-role-selection-*` |
| `--jui-ctl-*` | `--jui-comp-control-*` |
| `--jui-btn-*` | `--jui-comp-button-*` |
| `--jui-tabset-*` | `--jui-comp-tabset-*` |
| `--jui-container-*` | `--jui-comp-container-*` |
| `--jui-state-*` | `--jui-role-focus-*`, `--jui-role-disabled*`, `--jui-role-feedback-error`, surface-error, etc. |
| `--jui-border-radius`, `-soft`, `-hard` | `--jui-radius-*` |
| `--jui-color-tertiary*` | `--jui-color-ink*` |
| `--jui-color-aux-focus1` / `-focus2` | `--jui-color-info40` / `-info20` (or the `--jui-role-focus-*` tokens) |
| `--jui-color-aux-blue` | `--jui-color-info50` |
| `--jui-frag-card-outlined-*` | deprecated; migrate to the card component's `--cpt-*` tokens |

These exist for compatibility only. Do not add new tokens here, and don't build new features on legacy names.
