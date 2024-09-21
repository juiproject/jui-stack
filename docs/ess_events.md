# Events

Events are a fundamental mechanism that promotes notification and response to various changes in state across components without tight coupling those components.

There are two styles of event management: dispatching and listeners. The listener model is being deprecated in favour of dispatchers, however both are presented here.

## Standard event model

*Events in this context are **different** from UI (i.e. browser) events. These events are events generated directly by some class (instance) to indicate changes defined in context of that class.*

The default for observing events from objects in JUI is for the object to expose a mechanism that allows for registering event handlers (external) and to override an event method (sub-classes). This takes the generalised form (multiple events just repeat the pattern):

```java
public class SomeObject {

    public SomeObject handleOnSomeEvent(Consumer<SomeObject> handler) {
        ...
    }

    protected void onSomeEvent() {
        ...
    }

}
```

When an event occurs inside the object (instance) `onSomeEvent()` is invoked (along with any override) along with all handlers registered via `handleSomeEvent(...)`.

In terms of practical use the above is all you need to know and JUI classes will make clear what events are supported and under what circumstances. The remainder of the section describes how dispatcher are used to implement this mechanism.

*Dispatchers are relatively new and are superceeding the previous listener model. You should still familiarise yourself with this older model as it is continues to be widely used, see [Observation](#observation).*


### Dispatchers

The `Dispatcher` class provides a simple mechanism for dispatching values to listeners. To illustrate consider the following:

```java
Dispatcher<String> dispatcher = new Dispatcher<>();
dispatcher.add (value -> {
    Logger.info ("Value = " + value);
});
dispatcher.dispatch ("A");       // Dispatch 1
dispatcher.dispatch ("A");       // Dispatch 2
dispatcher.dispatch ("A", true); // Dispatch 3
```

Running the above will result in the following console log output:

```bash
[INFO] A
[INFO] A
```

The first comes from dispatch 1 while the last comes from dispatch 3. Dispatch 2 is blocked since it was the same as the prior dispatch while dispatch 3 is not blocked as it is forced (as directed by the second argument).

That essentially all that dispatchers do. It is worth noting that sometime one may which to add a handler only to want to remove it later. This can be achieved by a call to `addWithHandler(...)`:

```java
Dispatcher<String> dispatcher = new Dispatcher<>();
IRegistrationHandler handler = dispatcher.addWithHandler (value -> {
    Logger.info ("Value = " + value);
});
dispatcher.dispatch ("A");
dispatcher.dispatch ("B");
handler.remove ();
dispatcher.dispatch ("C");
```

which generates the following:

```bash
[INFO] A
[INFO] B
```

The last dispatch is not processed as the handler has been removed.

### Dispatches for events

The following model is employed for event dispatching. Consuder a class `MyObject` that you want to respond to a simple change event:

```java
public class MyObject {

    private Dispatcher<MyObject> changeDispatcher;

    public MyObject handleOnChange(Consumer<MyObject> handler) {
        changeDispatcher = Dispatcher.add (changeDispatcher, handler);
    }

    protected final void _onChange() {
        // The dispatch value is the instance of this class, so we
        // need to force the dispatch (as this value never changes).
        Dispatcher.dispatch (changeDispatcher, this, true);
        onChange ();
    }

    protected void onChange() {
        // Nothing (for override).
    }

    ...

    public void someMethodThatMakesAChange() {
        ...
        _onChange ();
    }
}
```

The idea is that (internally) one invokes `_onChange()` to fire a change event. This invokes the method `onChange()` which caters for sub-classes (they just override this) as well as dispatching to registered listeners. To register a listener on calls `handleOnChange(...)` passing a suitable lambda-expression. This gets recorded by the internal dispatcher for dispatching. Note the use of the static support methods `Dispatcher.add (...)` and `Dispatcher.dispatch (...)` which simply allow for one not to create an explicit instance of a specific dispatcher until it is actually needed.

In this example the dispatched value is the object itself (which is why it is forced). Sometimes you want to dispatch a value separate from the source but still capture the source. The `Dispatcher` declares a convenience class `Event<S,V>` for this that carries a reference to the source and the value. We could modify the above example to make use of this:

```java
public class MyObject {

    private String value;

    private Dispatcher<Event<MyObject,String>> changeDispatcher;

    public MyObject handleOnChange(Consumer<Event<MyObject,String>> handler) {
        changeDispatcher = Dispatcher.add (changeDispatcher, handler);
    }

    protected final void _onChange() {
        // Here we dispatch the event that wraps this (as the source)
        // with the value.
        Dispatcher.dispatch (changeDispatcher, new Event<MyObject,String> (this, value), true);
        onChange ();
    }
}
```

When employing this approach for your own purposes think carefully as to the necessity of it. In this example the value could have just been obtained directly from the source. In general you will only want to pass through transient data that is pertinent only to the event.

## Observation

Something that emits events should implement `IObservable` which provides a mechanism to allow for the registration of events. An event is declared by way of an interface (more specifically the events are methods on the interface) and a listener is an implementation of the interface. One then registers that implementation with an `IObservable` by calling the observables `addListener(...)` method.

```java
IObservable beingObserver = ...;
beingObserved.addListener(new IMyListener () {
  public void onSomething() {
    Logger.log ("Something happened");
  }
};
```

The above code illustrates how a listener is added to an observable so when the observable fire the `IMyListener.onSomething` event the registered instance will receive that event as an invocation of the `onSomething()` method (and in the example will generate a log entry to the console).

As a matter of convenience anything that needs to be an observer can extend `Observable` (`Component`, for example, extends this). When the observable wants to fire an event it can simply invoke the `fireEvent(...)` method:

```java
public class MyObservable extends Observable {
  public void someMethod() {
     ...
     fireEvent (IMyListener.class).onSomething ();
     ...
  }
}
```

Here the call to `fireEvent(...)` is passed the class of the registered listener and what is returned is an instance of that class for which one can invoke the method corresponding to the event (this returned instance has been implemented to invoked the same method on all registered listener of that type).

### Obsevation with listeners

As described above to register a listener against an observable one implements the listener interface that declares the events of interest and invokes `addListener(...)` on the observable. The same listener can be removed by calling `removeListener(...)`.

There is no harm adding listener to observables that don't fire associated events (i.e. this is not strictly enforced) which means that the observer-listener model is quite loosely coupled. This can be useful in that one can chain observables so that events fired from one observable are conveyed through other observables (an example of this is when one has a series of nested components wrapped in a dialog, the bottom level component can fire a close event which is conveyed all the way up to the dialog that can then respond to the event by closing).  This conveying of events is not automatic however and must be declared:

```java
IObservable childObservable = ...;
childObservable.convey(this, IMyListener.class);
```

Here the child observable is being asked to convey events declared in `IMyListener` onto this; listeners added to this will then receive these events.

When it comes to creating your own listeners you simple create an interfaces that extends `IListener` then add the events you wish to declare (the `IListener` is used to generate the event dispatch mechanism described next). Use of these is exactly as described above.

Some best practices when it comes to declaring event listeners are:

1. Try to keep the number of events per listener small (i.e. group only highly related events), this makes it easier to listen to specifics. This is not a hard-and-fast rule as sometimes it makes sense to bundle all events, especially when listeners will generally want to listen to all the events in which case disaggregation can lead to more code than less.
2. If you have a listener with a number of events and is widely used consider creating an abstract base class that implements all the events. Listeners can then extend that implementing only the events they want to respond to.
3. It is sometimes useful to employ lambda-functions for event listening. This can be facilitated by add static helper methods to listeners that take a lambda and return a listener (this can reduce the amount of code required).
4. Currently listeners don't support default implementations (hence point (3) above). This may change in the future.

### Observation implementation

*This is optional and only provided for interest.*

Currently listeners may use of GWT rebinding (this is going to be replaced with Java Annotation Processing but has the same effect). During JavaScript compilation all classes that extend `IListener` are collected and implementations created that delegate through to a chain of registered listener. This results in an instance of `ListenerOracle` being created (and accessible through `ListenerOracle.instance()`). The `Observable` makes use of this orcale to dispatch events.

For the interested reader look at the implementations of `Observable` and `ListenerOracle`. All code is found under the `com.effacy.gwt.core.client.observable` package in the `gwt-stack-core` project.
