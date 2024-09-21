# Overview

This package contains classes that support `com.effacy.jui.core.client.dom.DomBuilder`. These fall into two categories:

1. Implementation classes (specifically to implement the DOM building mechanism).
2. Support classes (specifically to facilitate working with the DOM building mechanism).

The implementation classes are generally of the from `XXXBuilder` or `XXXContext` while the balance relate to supporting use (which extend from active DOM building to fragment creation).

# Processes

## Adding an element

To add an element:

1. Create a static create method on `DomBuilder`.
2. Add an equivalent method on `ContainerBuilder`.
3. Create a matching use class.

For example, adding the element HR:

```java
/* On DomBuilder */
/**
 * Inserts a standard DOM element.
 * 
 * @param configurer
 *                   configurer(s) for the newly created element.
 * @param nodes
 *                   (optional) nodes to append.
 * @return the element.
 */
@SafeVarargs
public static ElementBuilder hr(Consumer<ElementBuilder>... configurer) {
    return custom ("hr", configurer);
}

/* On ContainerBuilder */
/**
 * Inserts a standard DOM element.
 * 
 * @param configurer
 *                   configurer(s) for the newly created element.
 * @return the element.
 */
@SafeVarargs
public final ElementBuilder hr(Consumer<ElementBuilder>... configurer) {
    return With.$ (DomBuilder.hr (configurer), v -> insert (v));
}

/* New use class */
public class Hr {
    
    /**
     * Creates an instance of this element and applies the passed builders to it.
     * 
     * @param builder
     *                the builders to apply.
     * @return the newly created element.
     */
    @SafeVarargs
    public static ElementBuilder $(Consumer<ElementBuilder>... builder) {
        return DomBuilder.hr (builder);
    }

    /**
     * Creates an instance of this element, applies the passed builders to it and
     * inserts it into the passed parent.
     * 
     * @param parent
     *                the parent to insert the element into.
     * @return the newly created element.
     */
    public static ElementBuilder $(IDomInsertableContainer<?> parent) {
        ElementBuilder el = $ ();
        parent.insert (el);
        return el;
    }
}
```

# Technical description

## Implementation classes

### The DomBuilder

The `DomBuilder` (in the parent package) is just a collection of static methods for create instances of `ElementBuilder` (with rarefied exceptions). The general method pattern adheres to:

```java
@SafeVarargs
public static ElementBuilder <tag>(Consumer<ElementBuilder>... configurer) {
    return custom ("/* tag */", configurer);
}
```

where `/* tag */` is replaced by the DOM tag name. For reference the `custom` method is:

```java
@SafeVarargs
public static ElementBuilder custom(String tag, Consumer<ElementBuilder>... configurer) {
    return With.$ (new ElementBuilder (tag), configurer);
}
```

which creates the associated `ElementBuilder` and applies any passed configurers.

*When adding new DOM builders add an associated static method creator on `DomBuilder` in the same manner as described above.*

### The NodeBuilder hierarchy

The `NodeBuilder` class forms a hierarchy of classes whose instances are assembled during the *construction phase*. This structure consists of nested builders (see `ContainerBuilder` for a builder that contains child builders) each configured to generate a DOM node structure of specific character. As such the structures shadows the resultant DOM that will be generated.

Once the structure is in place the *build phase* begins where the root builders' `build(...)` method is called. This recursively builds out a DOM structure based on the corresponding builder structure. The returned object (of type `NodeContext`) carries with it the root DOM node along with any registered event handlers, node that were mapped by reference and any objects otherwise lodge (such as components) for post-processing.

Collectively this model follows the build pattern and thus inherits in the moniker of builders.

#### Insertables

Ideally we want not to restrict our builder structures to only accepts builders, rather we want to allow for the insertion of other mechanisms of DOM construction. To facilitate this we make use of two interfaces: `IDomInsertableContainer` and `IDomInsertable`. The latter declares a method `void insertInto(ContainerBuilder)` which permits insertion of DOM builders into a container. The former provides insertion methods into the container for `IDomInsertable`'s.

In general, when a container receives an insertable for insertion it will resolve it immediately (i.e. by invoking `insertInto(...)`). Note that if the thing being inserted resolves by adding DOM nodes then it should either ensure that it is fully configured prior to insertion or wrap itself in a custom builder that defers resolution (see `Fragment`).

#### NodeBuilder

The aforemetioned `ElementBuilder` is a member of the `NodeBuilder` hierarchy. The `NodeBuilder` declares `NodeContext build(Consumer<NodeContext>)` which constitutes the *build phase* (this is done once the builder structure is in place, during the *construction phase*) that performs the following:

1. Creates an internal `BuildContext` instance.
2. Invoke the internal `_node(NodeBuilder,BuildContext)` method that peforms the node building.
3. Creates an instance of `NodeContext` wrapping te `BuildContext` created in (1).
4. Processes any consumer passed to the method (which allows for DOM extraction).
5. Returns the `NodeContext` created in (3).

The `BuildContext` context is used during the build process to allow nodes to register event handlers and node references (among other things). These data are then made available to the caller via the returned `NodeContext`.

The method `_node(NodeBuilder,BuildContext)` is the entry point into the actual build process for that node (it is final so not expected to be overridden). It invokes the abstract method `_nodeImpl(Node,BuildContext)` for the actual node construction and otherwise performs some post-node-creation activities (such as registering activities into the build context including node references for later lookup, event handlers and applying any post-build executions).

The `_nodeImpl(Node,BuildContext)` does the heavy-lifting of DOM building. It is passed a parent node that it may choose to render into, if it does not then it can return a node which the parent is expected to place accordingly. In general, if the builder is building a single node then it should return that node (and must do so if there is no parent; this is always that case for the root-level builder from which a root node must be obtained and returned by `NodeContext`).

#### ContainerBuilder

The `ContainerBuilder` extends `NodeBuilder` and provides a mechanism to add children (being other `NodeBuilder`'s) during the *node construction phase*. This addition is provided by `insertNode(NodeBuilder)` however is package accessible only. The reason for this is that `ContainerBuilder` implements `IDomInsertableContainer` which allows for the insertions of `IDomInsertable`'s. These have a method `insertInto(ContainerBuilder)` that knows how to insert itself into a `ContainerBuilder` as the parent. The `NodeBuilder` implements this via `insertNode(NodeBuilder)` which is currently the only way of concretely adding a child node. Other implementations of `IDomInsertable` are expected to insert DOM structures via `NodeBuilder` instances (this is how `Fragment`'s work).

Internally a list of `NodeBuilder`'s is maintained as the children. The implementation of `_nodeImpl(Node,BuildContext)` simply iterates over each child calling its `_node(NodeBuilder,BuildContext)` method and appending any returned node to the parent. In this way `ContainerBuilder`, by itself, simple manages the insertion of children into a parent (rather than necessarily representing a node itself).

#### ElementBuilder

The `ElementBuilder` extends `ContainerBuilder` and represents a concrete DOM element node (of a given tag name). It support a range of methods to assign attribute of various types and its implementation of `_nodeImpl(Node,BuildContext)` first creates a node of the given tag name, passes that element through to the super-classes implementation of `_nodeImpl(Node,BuildContext)` (this being that of `ContainerBuilder` which then adds in the children) and returns the built node.

#### TextBuilder

The `TextBuilder` extends `NodeBuilder` and it's purpose is to insert into the parent node text nodes. This does not return a node from `_nodeImpl(Node,BuildContext)` for the reason that it may insert multiple nodes (i.e when splitting text).

#### Renderer builders

These are builders specific to rendering from renderers (i.e. `IRenderer`, `IDataRenderer` and `IRenderable`). In general these rendering their contents into the parent (without returning a node) and register any UI event handler with the build context that is returned.

The `IRenderable` requires some additional explanation as it is extended by `IComponent` (in `jui-core`) and can be used to inject directly a component into the DOM. This is `lodged` aganist the `BuildContext` so that when the DOM building is building the DOM for a component the owning component can presses this renderable as a component thereby allowing that component to undergo management and adoption into the UI event lifecycle (and by-pass component attachement). Note that lodgement is abstracted to simple lodging objects with the expectation that the caller will know how to process them accordingly.

#### ExistingElementBuilder

This is used for the case where there is an existing parent that is being processed directly, in this case multiple builders can contribute to this node and the node is treated as being root.

Normally this type of node would extend `ContainerBuilder` (as noted this is transparent to the parent node duing build) however we want to treat it as `ElementBuilder` so that attributes can be assigned. This is achieved by setting the `ElementBuilder` tag name to `null` and having the `ElementBuilder` use the parent as the node rather than creating one.

This node is always expected to be a root node.

## Fragment classes

A fragment is a mechanism to generate DOM beyond a single node but rather as a structure of DOM nodes (the idea being that one can use fragments to generate UI design atoms).

The `Fragment` class implements `IDomInsertable` so that it can be added to a `ContainerBuilder` (see above). This is actually done by wrapping it in a `FragmentBuilder` that inserts itself (as `NodeBuilder`) and defers building the fragment until the *build phase*. The reason for doing this is so as to allow a fragment to be created and inserted into the parent builder before it is fully configured (the resolution of the fragment into builders would otherwise be performed on insertion thereby evading further confguration). This enables the use of fragments to be consistent with how the supporting use classes are employed:

```java
MyFragment.$ (parent)./* configuration*/.$ (/* children where applicable */);
```

Beyond this there is little that is difficult with fragments as they ultimately act like templates. With `FragmentWithChildren` one can also add child `IDomInsertable`'s (being either other fragments or builders). This implements `IDomInsertableContainer` so can operate identically as a builder.

## Use classes

These classes shadow DOM nodes and are standardised to provide two methods:

```java
@SafeVarargs
public static ElementBuilder $(Consumer<ElementBuilder>... builder) {
    return DomBuilder./* tag */ (builder);
}

public static ElementBuilder $(IDomInsertableContainer<?> parent) {
    ElementBuilder el = $ ();
    parent.insert (el);
    return el;
}
```

where `/* tag */` is the dom node being supported.

The classes are purely to facilitate expressive DOM creation:

```java
Div.$ ()./* configiration */.$ (/* children */)
```

for `IDomInsertable` insertion or

```java
Div.$ (parent)./* configiration */.$ (/* children */)
```

for direct `ContainerBuilder` insertion. The latter effectively replaces:

```java
parent.div (div -> {
    div./* configiration */;
    /* children */
});
```

to bring to the front of the line the tag being created.