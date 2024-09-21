# Modals (and dialogs)

?>[Lesson 4](lessons_4.md) coveres a range of practical cases where dialogs may be employed.

A framework for modals can be found under the package `com.effacy.jui.ui.client.modal` and consists of two main classes: `Modal` and `ModalDialog`. The `Modal` class provides a simple mechanism for presenting and managing modal encapsulated components while `ModalDialog` has a focus on dialog construction and content.

## Introduction

Modals (and dialogs) are a mechanism provided to "float" a behaviour out of the main application to focus and control attention and action to that behaviour. The most familiar forms of these are alert or confirmation dialogs as well as create and edit forms.

## Working with dialogs

### Simple dialog

The helper class `ModalDialogCreator` provides a number of mechanisms for easily building dialogs (which are instances of the component `ModalDialog` that are rendered outside of the normal application hierarchy directly into the page body), we begin by using a process whereby one "wraps" a component in a dialog:

```java
ModalDialogCreator.build (ComponentCreator.$ (p -> {
    p.css (CSS.MARGIN, Insets.em (2)).text ("Hello!");
}), mcfg -> {
    mcfg.removeOnClose ();
    mcfg.title ("Example dialog");
    mcfg.width (Length.px (300));
    mcfg.action (action -> {
        action.label ("Confirm");
    });
}).open ();
```

Here we create a simple inline component to wrap (this just displays the text "Hello!") in a dialog titled "Example dialog", of a fixed width of 300px and a single action labelled "Confirm" that closes the dialog. In addition we expand on a few points in the example:

1. The method `ModalDialog.build (...)` takes a component and a consumer that accepts an instance of `ModalDialog.Config` used to configure the dialog's behaviour.
2. The `removeOnClose ()` option will dispose of the dialog (and its component if the component is not referenced outside of the modal) removing it from the DOM. This allows one to open it and then forget about it (this is often fine for notifications but you will probably want to retain more complex dialogs such as forms).
3. The action declares no handler so, by default, will close the dialog.

With this approach we can be a little more sophisticated and create more complex custom dialogs:

```java
ModalDialogCreator.build (ControlPanelCreator.build (ccfg -> {
    ccfg.padding (Insets.em (1));
    ccfg.messagesStyles (Style.STANDARD_COMPACT);
}, panel -> {
    panel.section (null, sec -> {
        sec.group (gcfg -> {
            grp.control (0, 0, TextControl.create () //
                .name ("name") //
                .acceptor ("name") //
                .validator (NotEmptyValidator.validator ("please enter the name of the project")) //
                .build () //
            ).label ("Name of project").grow (1).required ();
        }).build ();
    });
}), cfg -> {
    cfg.title ("Create a project").width (Length.px (400)).closable ().removeOnClose ();
    mcfg.action (action -> {
        action.label ("Create");
        action.handler (hcb -> {
            if (hcb.contents().validate()) {
                String name = (String) ah.contents ().value ("name").get ();
                Logger.log ("name: " + name);
                hcb.success ();
            } else
                hcb.fail ();
        });
    });
}).open ();
```

This makes use of `ControlPanelCreator` to build a control panel (see [Controls](ess_controls.md)) adding a single text field. On action the form is validated, if invalid note the call to `hcb.fail ()`. This tells the dialog that the action failed to complete and the dialog needs to stay open. On success the call `hcb.success ()` tells the dialog that the action was successful and the dialog can close.

## Modals

With regard to presentation and management dialogs can appear in multiple forms (centrally located as is traditionally experienced and as a slide-out from the side of the screen) and can be layered (i.e. opening one dialog over another). The exact presentation mode is passed through the modals' configuration.

Internally `Modal` manages a static list of open dialogs, this is used to track assign the appropriate CSS so that dialogs can be appropriately layered.  It also makes available open and close handlers that one can externally track the opening and closing of dialogs.

Modal's don't partake in the normal render flow, that is, they do no form part of the main applications component hierarchy.  Rather they are rendered into a specially created container element attached to the document body. CSS styles are then used to position dialogs correctly. This does not affect event handler though as modal's are still components and thus integrate into the GWT browser event life-cycle.

A model is rendered only when it is first opened. Subsequent to that it is shown (on open) or hidden (on close, though some modal's are transient and when closed are removed from the DOM and disposed of). In terms of the contents of a modal this is provided during construction (either via a constructor or by overriding the `createContents()` method) as a component that is attached via an attachment point (see [Components](ess_components.md#attachment-points)) in the modal's DOM. Since this forms part of the components component hierarchy, it too will be rendered only when the dialog is rendered.

Some useful features of modal's include:

1. If the contents component implements `IOpenAware` it will receive a call to `IOpenAware.onOpen()` whenever the modal is opened. Similar for `ICloseAware`.
2. If the contents component implements `IModalAware` then it will receive a call to `IModalAware.onModalCloseRequested(IModalController cb)`; this can be used to block or defer the closing of a modal (i.e. to test for changes on the modal) where the close sequence will only continue if the callback is invoked.
3. The modal itself will generate open and close events conforming to `IModalListener`.
4. One may specify the global `Modal.BLUR_TARGET`; if set then that element will have a blur style applied to it. This can be used to blur the background of the page to bring a stronger visual focus to the modal contents.
5. The `IModalController` listener will be added to the contents component so the component firing a `IModalController.close()` event will activate a close of the dialog (i.e. mimics a close action).
6. Setting `removeOnClose ()` on the configuration will dispose of the modal when it is closed (rather than retaining it to be opened later).

As a final observation `Modal`'s only provide a very minimum of presentation (sufficient to position the modal) so the containing component is expected to provide all visual presentation and the mechanisms for control (i.e. closing the modal). In general, should one want a more traditional modal *dialog* then one should use `ModalDialog` as described below.

## Dialogs

Dialogs are implemented with `ModalDialog` (which extends `Modal`) and these provides additional capabilities such as an adornment (i.e. a window-like containment area that the contents are displayed in) and actions (close action and a place for custom buttons).

For dialogs the most distinctive aspect over that inherited from `Modal` is the handling of actions.  Actions appear as buttons at the foot of the dialog and are largely configured through the configuration. Just prior to rendering `ModalDialog` will invoke `populateActions()` which (by default) populates actions as declared in the passed configuration. An example follows:

```java
public static class MyDialog extends ModalDialog<TextComponent> {
    public MyDialog() {
        super (new ModalDialog.Config ()
            .title ("Example dialog")
            .width (Length.px (400))
            .closable ()
            .removeOnClose (),
            new TextComponent ("This is an example dialog")
        );
        config ().action ("dismiss", false, null, true, cb -> {
            cb.success ();
        });
        config ().action ("Open another", false, null, false, cb -> {
            new MyDialog ().open ();
            cb.fail ();
        });
    }
}
```

This dialog encapsulates an instance of the `TextComponent` component (this is found in the playground, it is used to display some text for demonstration purposes). As can be seen from the configuration it declares a title "Example dialog", has a fixed width of 400px, is closable (a close action displayed at the top right of the dialog window) and is marked to be removed on close. This latter point allows the dialog to be invoked by `new MyDialog().open()` an not having to worry about disposing of the component instance.

In addition two actions are configured, one that when clicked dismisses the dialog, the other opens a new dialog on top of the existing one. Note that each action declares a handler which is passed a callback. One may call `success()` or `fail()` on completion of the action and that will result in a closing of the dialog (success) or keeping it open (fail). This nomenclature is based on the assumption that an action on a dialog, when invoked, will typically result in the dialog completing its purpose and being closed.

One can also declare action handling by reference, the following is a variation on the above but uses action references:

```java
public static class MyDialog extends ModalDialog<TextComponent> {
    public MyDialog() {
        super (new ModalDialog.Config ()
            .title ("Example dialog")
            .width (Length.px (400))
            .closable ()
            .removeOnClose (),
            new TextComponent ("This is an example dialog")
        );
        config ().action (a -> a.label ("dismiss").reference ("close").link ());
        config ().action (a -> a.label ("Open another").reference ("open"));
    }

    @Override
    protected void handleAction(Object reference, IActionHandlerCallback cb) {
        if ("close".equals (reference)) {
            cb.success ();
        } else if ("open".equals (reference)) {
            new MyDialog ().open ();
            cb.fail ();
        }
    }
}
```

Referenced actions also have the ability to be shown, hidden, enabled and disabled. They can respond to state changes in the contents of the dialog. In closing out this discussion on actions it should be noted that actions can be declared directly using the `addAction(...)` family of methods by overriding `populateActions()`.

Some useful features of dialogs include:

1. A title and sub-title can be assigned during configuration.
2. In addition to the display mode (from `Modal`) a style can be applied. Custom styles can also be developed and assigned by an implementation of the `Style` interface (to create one's own style follow the examples provided with the `ModalDialog`).
3. Supports declarative actions (as described above), however this is not a requirement and if no actions are declared no action footer is displayed (in which case the contents component needs to provide all related functionality).

We now move onto helpers for creating and dealing with dialogs.

## Dialog helpers

### Build support

The `ModalDialogCreator` us the helper class for dialogs and declares a number of static support methods. As per the usual component creator patter the `build` static method can be used to create a dialog wrapping another component:

```java
ModalDialogCreator.build (ComponentCreator.$ (p -> {
    p.css (CSS.MARGIN, Insets.em (2)).text ("Hello!");
}), mcfg -> {
    mcfg.title ("Example dialog").width (Length.px (300)).removeOnClose ();
    mcfg.action (action -> {
        action.label ("Confirm");
    });
}).open ();
```

### Wrapping components

Dialogs are often re-used and invoked from multiple locations (whether from different actions on a single page or across multiple pages). To facilitate this JUI has the `IDialogOpener` (declared in `ModalDialogCreator`) with construction methods on `ModalDialogCreator` that can wrap a component turning it into a dialog.

More specifically `IDialogOpener<V1,V2>` where `V1` is an *assignment type* while `V2` is a *resultant type*. The opener declares a method `void open(V1 value, Consumer<Optional<V2>> cb)` which is passed an instance of the assignment type (which is used to configure the initial state of the dialog) and a handler for processing any result from the actions on the dialog. Note that the result is typed over `Optional`; the intention being that there is no result when the dialog is cancelled (or closed), in which can no auxillary action should be taken.

We can see how this operates through a typical scenario: editing some record of information:

```java
Record r = ...; // Obtain record to edit.
EditRecordDialog.open (r, outcome -> {
    if (outcome.isPresent())
        replaceRecord (outcome.get());
});
```

In the case of a create we can set `V1` to `Void` and pass `null`. We can also safely pass `null` to the handler.

`ModalDialogCreator` declares a family of `dialog(...)` methods that perform the heavy lifting of creating a `IDialogOpener`. These method expect a component to be passed that form the contents of the resulting modal, the usual pattern of inclusion is to *dialog enable* the component:

```java
public class MyUpdateForm extends ... {

    private static IDialogOpener<V1, V2> DIALOG;

    public static void open(V1 value, Consumer<Optional<V2>> callback) {
        if (DIALOG == null) {
            DIALOG = ModalDialogCreator.<V1, V2, MyUpdateForm>dialog (new MyUpdateForm (), cfg -> {
                cfg.maxWidth (Length.px (500));
                cfg.title ("Update something");
            }, cancel -> cancel.label ("cancel"), apply -> apply.label ("Update"));
        }
        DIALOG.open (value, callback);
    }
}
```

This allows us to open the dialog with `MyUpdateForm.open (...)` that has a cancel button and an update button (this update button is assigned the default action handler).

The obvious question begs: how does this dialog construct interact with the component being wrapped? The answer is via interfaces and events. In particular:

1. If the component implements `IEditable<V1>` then the `edit(V1)` method is invoked with the value passed to `open(V1,...)` (if the value implements `IResolver` then `edit(IResolver<V1>)` is invoked).
2. If the component implements `IResetable` then `reset()` is invoked on open (but after any value assignment).
3. If the component implements `IProcessable` then the `process(...)` method is invoked (this comes by way of the default action handler that is assigned to the update action).
4. If the component fires an `IUpdateListener.onUpdate(...)` event then that will trigger the callback (without closing the dialog). Similarly for `IValueChangeListener.onValueChanged(...)` except that the dialog *will* be closed.
5. If the component fires an `IModalListener.close()` even then the dialog will close.

One also some some control over the actions. In the example above the `dialog(...)` method took two configurers for a cancel and an update action. The cancel action is, by default, configured to close the dialog (and pass an empty `Optional` to the callback). The update action is, by default, configured with the `default` action (as noted in (3) above) and closes the dialog if the action completes successfully.

A variant of the `dialog(...)` is passed a configurer over `IActionConfiguration` which allows you more control over the actions.


## Notification dialogs

The class `NotificationDialog` provides for a number of utility method to create a variety of notification dialogs. These types include:

1. **Confirmation** to confirm an action or state of affairs, but giving the option to back out (i.e. cancel).
2. **Alert** to indicate some status that needs attention. No subsequent actions are implied (so one only dismisses the dialog).
3. **Error** being a variant of (2) but indicating an error.
4. **Save** for use when an action takes one away from something that needs confirmation (i.e navigating away from a form that has been modified). The implied actions are to save, discard or cancel (being that one goes back to the "form").

The following is an example of displaying a confirmation dialog:

```java
NotificationDialog.confirm ("Confirmation")
    .notice ("Are you sure you want to do this?")
    .handler ((outcome, done) -> {
        if (OutcomeType.OK == outcome) {
            /* do the action */
            done.complete ();
        } else
            done.complete ();
    }).open ();
```

Note that the handler accepts both an outcome (which is tied to the button) and a callback which, when completed, will close the dialog. You can omit the callback and the dialog will close on return from the action.

The following describes the buttons that are available and the associated outcomes:

|Button|Confirmation|Alert|Error|Save|Outcome|
|------|------------|-----|-----|----|-------|
|Dialog close|Yes|Yes|Yes|Yes|`DISMISS`|
|Cancel|Yes|No|No|Yes|`DISMISS`|
|Dismiss|No|Yes|Yes|No|`DISMISS`|
|OK|Yes|No|No|No|`OK`|
|Discard Changes|No|No|No|Yes|`DISCARD`|
|Save|No|No|No|Yes|`OK`| 

