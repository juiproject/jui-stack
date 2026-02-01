# Stateful components

Some UI frameworks associated a component with a *state* such that when the state changes the component also changes to reflect that state. JUI offers a similar mechanism through the use of the `StateVariable` and the `StateComponent`.

The `StateVariable<V>` is a generic over the state type `V` and offers a number of methods to set and retrieve state. The state variable also provides a listener mechanism that allows listeners to be notified of changes in the state variable.

A `StateComponent` can be constructed with an internally declared state or one that is provided. It adds a listener to the variable and when it detects that the variable has changed it re-renders.

The following is a very simple (albeit contrived) example of the state variable in action (see part C of [Lesson 3](lessons_3.md)):

```java
public class MyButton5 extends StateComponent<String> {

    public MyButton5(StateVariable<String> label, Invoker handler) {
        super (label);
        renderer (root -> {
            ButtonCreator.$ (root, btn -> {
                btn.label (state ().value ());
                btn.handler (() -> handler.invoke ());
            });
        });
    }

}
```

Here the component expects to be passed an instance of `StateVariable<String>` that contains a string which will be used as label for the included button.  Whenever the state variable is assigned a new value the button component will re-render and this present a button with an updated label.

### Alternatives to re-rendering

When using `StateComponent` changes to the associated state variable result in the invocation of `onStateChanged()`. The default behaviour is to re-render, however one can override this method and respond differently (for example, by manipulating the DOM directly).

### Anatomy of the state variable

#### Assigning values

There are two means by which a value can be assigned:

1. Using the `assign(V)` method which is passed a new value and that value is assigned to the state variable (so will be returned by `value ()`).
2. Using the `modify (Function<V,V>)` method which is passed a function that maps the current value to a new value that is assigned. This is useful from complex state variables that you may want to modify rather than replace. In this case the function just modified and return the passed value.

Both of these method will invoke a change in the state variable and will notify the listeners (the exception being `assign(V)` that will only update if the passed value is not the same as the current variable by way of equality).

#### Additional states

The `StateVariable` goes beyond just maintaining a single custom state variable and announcing changes to it, it also maintains a supplementary state which reflects the lifecycle of the state variable (particularly when the variable is being updated remotely).

There are four supplementary state:

1. `OK` (see `isOk()` and `ok()` on `StateVariable`). This means that the state is well defined and can be used. This is the default state when the state variable is first created with an initial value.
2. `LOADING` (see `isLoading()` and `loading()` on `StateVariable`). This means that the state is loading (asynchronously) a revised value (or loading the first value). Generally a component will render some form of loading indicator. If the state variable is instantiated without an initial value (i.e. a `null` value) then it will be assigned this loading state.
3. `ERROR` (see `isError()` and `error(List<String>)` on `StateVariable`). This means that there was a problem assigning a state variable (i.e. an error loading the data). Note that passed is an optional list of errors which can be retrieved from the state variable for display. Since error messages arise in many context `StateVariable` comes with a convenience function `convert(List<V> errors, Function<V,String>)` to map errors.
4. `UNEXPECTED` (see `isUnexcepted()` and `unexpected()` on `StateVariable`). This means that the value was supplied in a non-error state but was not expected. By default this is the state should the state variable be assigned a `null` value (unless it is declared as nullable by a call to `nullable(boolean)`).

These states can be tested for when rendering:

```java
public class MyButton5 extends StateComponent<String> {

    public MyButton5(LifecycleStateVariable<String> label, Invoker handler) {
        super (label);
        renderer (root -> {
            if (state ().isError ()) {
                // Render the error state.
            } else if (state ().isLoading ()) {
                // Render the loading state.
            } else if (state ().isUnexpected ()) {
                // Render the unexpected state.
            } else {
                // Render the default (OK) state.
                ButtonCreator.$ (root, btn -> {
                    btn.label (state ().value ());
                    btn.handler (() -> handler.invoke ());
                });
            }
        });
    }

}
```

A value is assigned to a state variable by calling `assign(V)` and (given the commentary on point (4)) will transition to the `OK` state. Here is an example when using remoting (see [Remoting](topic_remoting.md) for details on the remoting mechanism):

```java
...

// Invoke a remote load.
new ApplicationServiceHandler<XXXResult> ()
    .onSuccessful (v -> {
        // On success assign the new value to the state variable (this will
        // refresh the component).
        state ().assign (v);
    })
    .onBefore (v -> {
        // Marke the state as loading (this will refresh the component).
        state ().loading ();
    })
    .onFailure ((errors, t) -> {
        // On error transition the state to the error state passing the error
        // messages with a translator (from FieldMessage to String).
        state ().error (errors, v -> v.getMessage (), t.name ());
    })
    .remoteExecute (new XXXLookup ());

...
```

#### State listeners

Listeners can be added with `listen(Consumer<StateVariable<V>>)` (if added to a `StateComponent` the component will add a listener automatically).

When the state variable changes value (or changes its supplementary state) all listeners will be notified of the change.

## States and navigation

When a stateful component extends `StateComponent` and implements `INavigationAware` (or `INavigationAwareChild`) then we are able to prevent re-rendering when the component is not activated from a navigation standpoint.

This is controlled by `NavigationBehaviour` which is assigned through the constructor. The default is `BLOCK_AND_NOTIFY`.

Now it may be that such a component is navigation aware but is used in a dual-use context: where it is subject to navigation and when not. In the latter the native behaviour will block re-rendering as it will seem that the component has never been navigated to.

A general strategy is to pass through an `embedded` state:

```java
public class MyComponent extends StateComponent<...> implements INavigationAware {

    public MyComponent(boolean embedded) {
        super(..., embedded ? NavigationBehaviour.NONE : null);
        ...
    }

}
```