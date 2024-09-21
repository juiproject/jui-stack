# Overview

The renderer framework provides a mechanism to generate DOM structures that generate DOM from data; essentially is allows for the building of programmatic DOM templates.

# Meta-modeling

## Overview

Support is provided to allow one to model a UI component in a manner that is distinct from the DOM used to render it.  This allows UI concerns to be expressed in a manner that is decoupled from presentation and ensure UI components are utilized (particularly those that deal mostly with rendering of data) in accordance with intent.

An example would be a simply summary layout of information.  This may consist of headings, content lines, paragraphs, properties (label and value pairs) and collapsible sections.  This is a model that can apply to any sort of data so long as one maps the data to the model elements.  In this context we should be able to represent the model akin to the following:

```
  renderer = new MyModelRenderer<DataType>();
  renderer.heading(d -> d.name());
  renderer.property("Status", d-> Labels.for(d.geStatus()));
  renderer.property("Data", d-> FormatSupport.format(d.getDate())).condition(d->d.getDate() != null);
  
  ...
  renderer.render(el, data);
```

Here the renderer follows a model and is configured for the specified data type (in this instance to display a heading followed by two properties, the last being conditional on being present).  The developer does not need to concern themselves with how this model is set out (i.e. presented) and the presentation is handled internally.

## Patterns

### Reuse pattern

These model items make ideal candidates for re-use; allowing them to be included in other model items.  A simple pattern that supports this is to consider a top-level model item and scope the CSS to that item.



Here the top-level item is declared and extends ``StylesBuilderItem`` (which provides support to pass through styles thereby decoupling the items from any specific context).  The item scopes classes for its model elements as well as encapsulating the styles for this and its children.  A special no-argument constructor is declared that pushes through a styles instance thereby allowing the item to be used directly in a renderer.

When it comes to child items one can declare these as inner classes so long as the data type carries through (in which case they can extend ``BuilderItem`` and draw styles from the encapsulating class instance).  If they are likely to become re-useable themselves then they should be declared static and extend ``StylesBuilderItem``.  Having said that, if you want to scope enums in the items then they need to be static so you may have no choice in the matter (and it really doesn't make that much of a difference).

When it comes to re-using then have the including item extend the CSS for the included item(s).


This can be done more multiple inclusions.  It should be important to note that care needs to be taken to ensure there are no name clashes for CSS styles.  A practice is to name a root style distinctively (i.e. ``myitem``) then scope each sub-style (i.e. ``myitem_selector``, ``myitem_open``, etc).

A final note on the CSS and that is the the above the CSS extends ``IComponentCSS``.  This is entirely optional and allows for a component to includes the CSS directly onto an ``AbstractCSSPresenter``.  You can also scope by ``.component`` in any of the included items (so affords some protection against CSS obfuscated name clashing).  However it could just extend ``CSSResource`` without too much trouble.


# Classes

The classes that constitute the builder frameworks are described below.

**Condition** and **ConditionBuilder**

`Condition` is used to test a condition on some data. These are used to control the rendering of DOM elements (conditionally) or application of attributes (also conditionally). To make it easy to build a variety of condition there are static methods on `ConditionBuilder` to cater for a range of scenarios.

**Provider** and **ProviderBuilder**

`Provider` is used to extract information from some data structure. Typical uses are to supply text content to populate to a DOM element or supply a CSS class name contingent on the data being used generate against. To make it easy to build a variety of condition there are static methods on `ProviderBuilder` to cater for a range of scenarios.

**IDataRenderer** and **IRenderer**

`IDataRenderer` defines the contract to expose a mechanism to render DOM content contingent on some data. It exposes a `render(Element,D)` method to which an element and configuration data can be passed. The implementing instance will then render a structure into the passed element configured against the passed data.

Also exposed is an event handler `event(UIEvent, Observable, String)` that can be used to pass through browser events (though this is dependent on how the renderer is being used so there is a default implementation that does not handle any event).

`IRenderer` is a variant of `IDataRenderer` but is not contingent on any passed data (any contingencies are determined internally). A static method is provided to convert a `IDataRenderer` whereby `null` is passed as the data argument.

**DOMDataRenderer**

A concrete implementation of `IDataRenderer` that incorporates a template DOM model and a template cache.

The template DOM model is oriented around the `Node` class the incorporates a condition (to determine if it should render or not) and a special loop condition which is explained later.  The node exposes a `render(SafeHtmlBuilder, D, ...)` method that is invoked to render a DOM structure (noting that the rendering is into a `SafeHtmlBuilder` which is quite fast and not dependent on any underlying element model - i.e. will work with Elemental2 or GWT). This method is final so not expected to be overridden. It first checks any condition against the passed data (and falls out if the test fails). It then delegates to `renderImplControl(SafeHtmlBuilder, D, ...)` which can be overridden (more on this later) which in turn delegates to an abstract method `renderImpl(SafeHtmlBuilder, D, ...)`. It is this method that is responsible for generating content.

A simple sub-class of `Node` is `Text` that contains a content provider with some additional contingent configuration for formatting. This implements `renderImpl` and simply calls one of the `appendHtmlConstant`, `appendEscapedLines` or `appendEscaped` based on the supplied and evaluated configuration and provided content.

A more complex sub-class of `Node` (and is the basis for all elements) is `Container`. This contains a list of child `Nodes`'s and an implementation of `renderImpl` that iterates over each of the child nodes invoking their respective `render(...)` methods. It also as the ability to loop. A loop is declared by a providing that takes the passed data (to the node) and extracts a list of items from it (of some type). If present the list of items is extracted in the `renderImplControl` method which iterates over each item pass said items to the `renderImpl` method as the methods data (to supply a loop provider one calls the `loop` method that provides the relevant type casting). The effective repeats the rendering of the container for each of the items in the list (hence the notion of a loop). This is often used to render a variable list of items. Information about the state of the loop (first element, last element, etc) is passed through as a loop context and any provided loop condition (see `LoopCondition`) can be used to condition rendering (just as for a vanilla condition).  Another variation is to capture this loop context is a wrapper (see the `loopWithData` method).

The `Container` can be used in its own right but will be contributory (that is, does not define any wrapper element). As such it is rarely used in this context (other than to implement some technical behaviours). It is the rendering of elements that is most relevant (and `Container` support a range of methods to create and insert element nodes).

Extending `Container` is `Element` that is configured with a tag name (that represents the element type as expressed in HTML) and provided values for CSS classes, attributes and direct CSS via the elements `style`. `Element` provides an implementation of `renderImpl` that creates the start and end tags and with calls the super class version of the same method to write in the child nodes.

What we haven't yet described is how a template is generated. As noted above the basis for the template is a structure based around `Node`; so a template is just a single `Node` instance with some hierarchy below it. `DOMDataRenderer` makes use of an `INodeProvider` to supply the template node and it uses that to render. So when a call is made to `render(...)` that method makes a call to `root(...)` that itself makes a call to `INodeProvider.node(...)` on an internal instance of `INodeProvider` to obtain the template node.  Passed to `INodeProvider.node` is a lambda call to the `template()` method that creates a single `Container` instance and passed that to `build(Container)`. One will generally override this method to generate the template programmatically against the passed container which then becomes the template node (one can also pass a separate `IDOMDataRenderBuilder` and that will be used to perform the template build).

Now the above seems a little convoluted. Ostensibly for every instance of `DOMDataRenderer` (when used in a component, for example, each instance of the component will likely have its own instance of a renderer unless the renderer is supplied externally, which is awkward) one will have a instance of the template node thus resulting in multiple copies of the template (one for each instance of the component). This is not very efficient and (for galleries for example) can consume a lot of time and memory.  Ideally we want to generate the template once and share that instance across components (or context). This is where `INodeProvider` comes in (see its description for details). This provides a means to cache nodes and when constructed with a cache key will only invoked its implementor once caching (statically) the result to be shared among all `INodeProvider` instances configured with the same cache key. Any desired cache key is passed through the constructor of `DOMDataRenderer` (if not passed then we fall into the scenario above and templates are generated for each instance, but this can be desireable in certain circumstances).

A note on the above is that when employing this renderer in a component it is advisable to uses the class name as the cache key (so allows for sub-classing without clashing templates).  Where CSS styles are used, if those can be varied (for differing themes) then it is wise to include additional data in the cache key to keep clashes from happening.