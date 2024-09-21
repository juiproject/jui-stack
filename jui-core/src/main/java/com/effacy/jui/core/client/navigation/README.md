# Navigation

Contains all classes related to navigation flows. Detailed usage documentation is provided in the **documentation hub**, including an outline of the underlying implementation.

This document provides further detail pertinent to maintenance of the mechanism or deep implementation of the principles into navigable structures (where the usage documentation in the **documentation hub** is insufficient). You should refer to this in the first instance, especially surveying the implementation section (that should given you a good sense of the flow of control through the mechanism) and the model (that describes the underlying principles employed).

As an aside the concepts behind navigation are quite nuanced however there is actually very little code involved. You are encouraged to look at `NavigationHandler` which implements the bulk of the behaviour.

## Class descriptions

### `INavigationHandler`

Represents a sub-tree in a navigation hierarchy provides tools to operate on that tree:

1. `navigate(NavigationContext, List)` to activate the nodes corresponding to the references in the passed path. Allowance is made for references not to exist and for the path to not fully resolve to a terminal node; in these exception cases a default sibling is always determined.
2. `renavigate(NavigationContext)` to re-assert the current navigation path (including all defaults).
3. `assignParent(INavigationHandlerParent)` used to insert the tree as a sub-tree of an encompassing hierarchy. See `INavigationHandlerParent`.

The `NavigationContext` is used to pass through context data (how the navigation arose) as well as how the mechanism should behave. Some of this context information is not used by mechism, that which is:

1. `refresh` which forces nodes that implement `INavigationAware` to receive a call to `onNavigateTo()` even when they were already active (this occurs on back propagation, see `NavigationHandler.backPropagate(...)`).
2. `backPropagateIfNotChanged` indicates that the mechanism should back propagate even when there were no changes in the configuration of active nodes; this takes precedence over `refresh`.
3. `changed` is intended for internal use, or when a custom node needs to indicate that there was a change in navigation. It is set when there has been a change of node and triggers back propagation (if not set the back propagation will only occur if `backPropagateIfNotChanged` is set).

Although one can create one's own implementation it is generally expected that one used `NavigationHandler`.

### `NavigationHandler`

The standard implementation of `INavigationHandler` and is assumed to be the implementation employed.

### `INavigationHandlerParent`

This represents the navigation hierarchy that sits above a handler (a parent is registered with a handler via `INavigationHandler.assignParent(INavigationParent)`). It is used to receive notification of back propagation (by calling `INavigationHandlerParent.onNavigation(NavigationContext, List)`). See `NavigationHandler.backPropagate(NavigationContext, I, List)` as well as `NavigationHandler.register(T)` where the handler assigns itself as parent to the item being registered.

This is also used to receive the termainal back propagation path that can be used to update the UI with the resolved path. For convenience the static method `INavigationHandlerParent.navigation(BiConsumer<NavigationContext, List>)` is provided.

### `INavigationItem`

Represents a node in the navigation hierarchy (and is what is registered with `NavigationHandler`). Generally this is delegating class to the underlying component held within a parent component.

An item has an enabling state (allowing it to be passively removed from the hierarchy) and the ability to be activated (as noted this tends to delegate through to the underlying components). Activation completes by way of a promise which enables the item to asynchronously load (for example, when code splitting is employed) prior to continuing with the navigation event (ensuring that the item is fully configured prior to forward propagating through). Note that activation can also occur on the back propagation and this is controlled via `activateOnForwardPropagation()`.

This extends `INavigationHandlerProvider` and `INavigationReference`. For the latter this is used to extract the string reference (which become the relative component in the navigation path) as well a abstracting the path component to item matching. The former allows the item to act as an internal node and accept forward propagation. If `handler()` returns `null` the item is assumed to be terminal and back propagation begins.

### `INavigationAware`

This is implemented by items (see also `INavigationAwareItem`) or more generally by components that need to receieve navigation lifecycle events. We describe these in the context of `NavigationHandler`:

1. `onNavigateToPrepare(String)` which is called on forward propagation and prior to activation. Passed is the path component associated to the item. See `NavigationHandler.navigate(NavigationContext, List)`.
2. `onNavigateFrom(INavigateCallback)` is called on forward propagation when the item is being navigated away from (to one of its siblings). The callback allows for suspension of the navigation event to allow for intervention (such as verifying the user wants to discard changes). See `NavigationHandler.navigate(final NavigationContext, I, List)`.
3. `onNavigateTo()` is called on back propagation after activation.  See `NavigationHandler.backPropagate(NavigationContext, I, List)`.

Note that when adding components that implement `INavigationAware` the mechanism that binds the component to the item being registered (see `TabSet` for an example) must delegate through the events.

## Implementation overview

The navigation model is abstracted through a family of interfaces. A component that participates in navigation must implement `INavigationHandler` which is able to respond to (local) navigation requests via the `navigate(...)` methods. Two addition methods are present: `renavigate(...)` (to re-assert the current navigation and adjust if any of the items are unresolved or no long enabled) and `assignParent(INavigationParent)` that incorporates the component into an encompassing navigation hierarchy.

In general a *navigation handler* simply responds to navigation requests and adjusts accordingly. Often this will be a component that maintains an instance of the concrete handler `NavigationHandler` to which it delegates (see `TabSet` for an example). The `NavigationHandler`, besides implementing `INavigationHandler`, is parameterised over a class implementing `INavigationItem`. The handler contains navigable children of this type and associated with a string reference (extraction of this is via methods on `INavigationReference`) which is what appears in the navigation path. This is used to lookup and activate items during a navigation event. The `INavigationItem` interface exposes methods to activate the item, to pass through navigation residual (where the item is a terminal node, the residual is the portion of the path not processed) and to determine whether activation should be on the forward or backward propagation flow. In addition `INavigationItem` extends `INavigationHandlerProvider` which is used to extract an `INavigationHandler` from the item (where that item is not terminal on the navigation hierarchy).

When a navigation event occurs a call to `navigate(...)` is made to initiate the flow. When this is delegated to and instance of `NavigationHandler` the handler will attempt to map the first component on the path to an item (see methods on `INavigationReference`). Assuming an item is found (if it is not the subsequence children are discarded and the default item is retrieved) that item is activated (or deferred to the back-propagation, see `activateOnForwardPropagation()` on `INavigationItem`). If the item returns a handler (see `handler()` on `INavigationHandlerProvider`) then its `navigate()` method will be called on the derived child path obtained by removing the first component. Note that a zero length path will always passively deference the default item in a handler so that forward propagation only stops when a terminal item is encountered (one that does not return a handler). Once a terminal item has be reached the back-propagation begins and this involves the handler calling `backPropogate()` on the registered `INavigationParent` (note that `NavigationHandler` implements this and when an item is registered against the handler the items `assignParent(...)` is invoked passing the handler). This back-propagation is used to activate those items who activate on back-propagation and any parent registered to the root will often update the browser URL to reflect the path that is now active.
