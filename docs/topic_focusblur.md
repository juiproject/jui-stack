# Focus and blur

*We provide a detailed explanation of how focus and blur is managed using components as a context. This is an advanced topic related to the inner workings of JUI and not needed for day-to-day use where the requirements for focus and blur are suitably expounded upon in [Components](ess_components.md#focus-and-blur).*

## Overview

DOM elements may gain and loose focus (see [Focusing: focus/blur](https://javascript.info/focus-blur) for a more detailed exposition). For certain control elements the will result in an outline being applied (whose presentation can be modified by defining a style associated with the `:focus` pseudo-class). In general, though, focus and blur will generate associated focus and blur events and can be controlled programmatically via the `focus()` and `blur()` methods.

When it comes to components we need to respond to focus and blur not at the individual element level but to the component as a whole.

## Principles

With respect to a component (or any assemblage that requires management of focus and blur, though we describe this within the context of a component) the key challenge is synchronising the focus and blur state of DOM nodes that maintain focus and blur with the corresponding state of the component (or assemblage). A complicating factor is where two DOM nodes under the component transfer focus between them, the component should also maintain focus during the transition.

To elucidate the principles involved we consider the following scenarios:

1. The component is programmatically requested to gain focus or to loose focus (blur).
2. The component currently does not have focus and one of its DOM elements gains focus (i.e. through a user interaction).
3. The component currently has focus, meaning a DOM element under it has focus, that DOM element looses focus such that no other DOM element under the component gains focus.
4. The variation on (3) where another DOM element under the component gains focus (so focus is transferred between two DOM elements under the component).

With respect to (1) we assume that the component gaining focus does not already have focus (otherwise the action would have no effect). In this case it is relatively easy as the component needs to nominate a target DOM element to gain focus the programmatically directs that element to gain focus. The component then needs to update its internal focus state. A component that already has focus will presumably have a DOM element that has focus. Once again the component needs to blur that element and update its state.

Moving to (2) the component must listen to changes in focus on its DOM elements, this will allow it to receive the corresponding UI events. In this instance, when a focus event is received it simply updates its internal focus state. The case is similar to (3).

Finally we consder (4) which is the most tricky: focus transitions between DOM elements under a component. The reason this is tricky is that a blur event is created by the element loosing focus and a focus event is created subsequently by the receiving element. This can result in the component changing rapidly between blur and focus even though it logically never looses focus. The problem with this is that the component may take some action when loosing focus (and example of this is a selector that has an action element and selection elements, the transition from action to selection could result in a brief change of focus causing the component to close the selector prematurely). An approach to dealing with this is to defer the blur for a brief period of time and cancelling the blur should a focus event arise during the deferral period.

So with the above we have outlined some of the key considerations one needs to take into account when treating focus and blur at the composite level (i.e. a component). We now move to describing how focus and blur management is performed within JUI.

## In practice

Integrated focus and blur management is provided by the class `BlurAndFocusBehaviour`. This provides a collective of lifecycle methods that can be employed to provide proper treatment of focus and blur (and maintenance of the current focus state) as well as callback to implement focus and blur. We describe the interactions with this class in the context of a component, for which the principles should be portable to other contexts.  As such we taken it as given that every component maintains a single internal instance of `BlurAndFocusBehaviour`.

The interaction of the component against the behaviour consists of:

1. **Register DOM elements** The behaviour needs to know all the DOM elements in the component that can gain and loose focus (the method `manageFocusEl(Element)` provides for this; a version is also declared on `Component` for convenience and delegates to the behaviour). The behaviour will then register those DOM elements with the UI event mechanism to process focus and blur events.
2. **Programmatic focus and blur** When the component receives requests to focus and blur these are passed directly onto the behaviour to manage (so the component needs to do nothing more that delegete these). These facilities are provided by `focus()` and `blur()`.
3. **Delegate focus and blur events** The behaviour needs to receive the focus and blur events of the elements registered against it in (1). To facilitate this `BlurAndFocusBehaviour` implements `IUIEventHandler` so can be introduced into the standard chain of UI event processing (for `Component` this is performed in `onUIEvent(...)`).

In addition the component can query the current focus state directly against the behaviour (see `isHasFocus()`).

We now follow the flow of control through points (2) and (3) above, starting with (2). Suppose the component receieves a request to gain focus, which it passes through to the behaviour:

```plantuml
autoactivate on
Component -> BlurAndFocusBehaviour : focus()
autoactivate off
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : cancelDeferredBlur()
autoactivate on
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : deferredFocus()
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : renderFocus():if not cancelled
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : safeFocus(focusEl)
autoactivate off
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : updateFocusState()
autoactivate on
BlurAndFocusBehaviour --> Component
```

Note that we first cancel any deferred blur that is in progress then instigate a deferred focus (for similar protection). This proceeds to implementing a rendering of focus (the `Component` overrides this as a hook to apply a focus CSS style if one is available) which applies the focus state to the default focus element (nominally the first registered focus element).

The programmatic blur is processed similarly which completes the description on (3). We now turn to (4).

When a UI is passed to the component it will pass it through to the behaviour for processing. We begin with a blur event:

```plantuml
autoactivate on
Component -> BlurAndFocusBehaviour : handleEvent(blurEvent)
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : deferredBlur()
autoactivate off
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : onBlur()
autoactivate on
BlurAndFocusBehaviour --> Component
```

This simply instigates a deferred blur which will, when processed, invoke the `onBlur()` hook (along with firing a blur event). The `Component` overrides this hook to invoke `onBlurUI()` and eventially `onBlur()` as a hook. We now consider a focus event:

```plantuml
autoactivate on
Component -> BlurAndFocusBehaviour : handleEvent(focusEvent)
autoactivate off
alt blurScheduled
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : cancelDeferredBlur()
else !blurScheduled
autoactivate on
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : deferredFocus()
autoactivate off
BlurAndFocusBehaviour -> BlurAndFocusBehaviour : onFocus()
end group
BlurAndFocusBehaviour --> Component
```

Note the check for a deferred blur, which will cancel the blur thus leaving the behaviour unchanged (which is what we want when transitioning focus between DOM elements under the same component). Otherwise the flow is similar to that of blur.