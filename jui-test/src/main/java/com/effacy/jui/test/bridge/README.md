# GWT package

Contains support classes used for testing JUI under GWT.

## `GWT.create`

This method is scanned for by the GWT compiler and initiates rebinding of the passed class. We provide an implementation of this through the supply of a dedicated `GWTBridge` that is initialised with a call to `JUITestEnvironment.init(...)`.

Passed to the custom bridge is an instance of `Rebinder` as returned by `RebinderBuilder.build()`. This includes a number of standard rebinders but allows for the addition of others. A rebinder simple accepts a class and returns an optional instance of that class. Rebinders are tried in order of addition until one returns a class instance, that is then returned by `GWT.create(Class)`.

The rebinder mechanism and standard rebinders are found in the `com.effacy.jui.test.bridge.rebind` package. The default rebinders provide a minimal working case for each of the types being rebinded (so don't expect full functionality).

The rebinders also make use of [ByteBuddy](https://bytebuddy.net/) to support creation of rebinded classes. This does mean that there is a test dependency on this which may or may not be problematic (i.e. JPA makes use of byte-buddy).