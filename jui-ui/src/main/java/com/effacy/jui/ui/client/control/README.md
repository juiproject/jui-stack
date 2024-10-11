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

## Debug model

Debugging controls (both in terms of developing the control and using the control) can involve multiple dimensions (both topics and detail). The general (standard) approach (when debugging support is being provided) is to enable debugging globally (so can be activated in the application entry point) and to flag each of the dimensions available. In terms of a design pattern:

1. Create an inner enum `DebugMode` that declares the various debug dimension (on the basis that any combination of these can be active at any one time). Map each enum to a specific bit in an `int`.
2. Creare a static `int` called `DEBUG` in the control class.
3. Add a `set()` method on the enum in (1) that tests if the corresponding flag on the static `int` in (2) is present.
4. Add a static `debug(DebugMode...)` on the control that sets the corresponding flag(s) on the static `int` in (2).

A concrete example is presented below. In terms of usage one just assigns the debug profile with (4):

```java
MyControl.debug(DebugMode.DIMENSION1, DebugMode.DIMENSION3);
```

The aforementioned example:

```java
...

/**
 * Various debug modes.
 */
public enum DebugMode {

    DIMENSION1(1<<1),
    
    DIMENSION2(1<<2),
    
    DIMENSION3(1<<3);

    /**
     * Bit flag for the specific debug mode.
     */
    private int flag;

    /**
     * Construct with initial data.
     */
    private DebugMode(int flag) {
        this.flag = flag;
    }

    /**
     * Determines if the flag is set.
     * 
     * @return {@code true} if it is.
     */
    public boolean set() {
        return ((MyControl.DEBUG & flag) > 0);
    }
}

/**
 * Flag to toggle debug mode.
 */
private static int DEBUG = 0;

/**
 * Assigns the passed modes for debugging.
 * 
 * @param modes
 *              the modes.
 */
public static void debug(DebugMode...modes) {
    DEBUG = 0;
    for (DebugMode mode : modes) {
        if (mode == null)
            continue;
        DEBUG |= mode.flag;
    }
}

...
```
