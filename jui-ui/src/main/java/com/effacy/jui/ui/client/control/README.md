# Overview

Contained within are a collection of standard UI controls that can be used directly or modified (used as templates).  These cover the general suite of controls one expects to see in a form.

To describe the controls we make use of a standardised description language (explained below). This should be sufficient to fully elaborate on a controls behaviour and its configuration. Separate to this is the notion of styling, which is also described in the following.

## Classes

Controls follow the same pattern with regard to supporting classes as do components: the control class of the form `XXXControl` and implements `IControl`. The creator class is named `XXXControlCreator` and the documentation class `XXXControlDocumentation` (recalling the latter is for including in the `ComponentExplorer`).

## Styling

There are three avenues to styles (excluded are changes to the DOM structures which require changes the the control presented; this is best handled by creating a new control):

1. Changing the basic colours and geometry
2. Overriding the CSS of the core styles
3. Providing additional styles

The class `StandardTheme` contains a collection of parameters that cut across all controls. Using the replacement technique (see [top-level documentation](../../) for a description) one can provide an alternative theme.

Each control has a main CSS styles resource and one that is blank specifically for overriding (named `XXX_Override.css`). Again using the replacement technique you can provide for an alternative.  To make styling easier parameters are declared as `var` parameters in the `.component` CSS class (generally specified by evaluation from the `StandardTheme`) which can be modified by providing an alternative `.component`.

Providing alternative styles is another option and is described in the [component documentation](../../component/).
