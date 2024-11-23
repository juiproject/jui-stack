# Overview

This project provides the core classes needed to successfully perform a compilation using the desired compilation system and to do so without dependence with respect to the specific system within the rest of the JUI project.

# Compilation systems

## GWT

*The current source version is `2.12.1`.*

Here we describe the GWT compilation system as maintained by the [GWT project](https://github.com/gwtproject/gwt) (this is currently the only compilation system supported):

1. [Description](#description) of the compilation system and how it is incorporated into JUI.
2. [Upgrading](#upgrading) when a new version of GWT becomes available.
3. [Modifications](#modifications) that have been made to the core GWT code that has been migrated into this project.
4. [Pending decoupling work](#pending-decoupling-work) that is ongoing.
5. [GWT compilation process](#gwt-compilation-process) provides an (rough) outline on how the GWT compiler converts Java to JavaScript.

### Description

The GWT compilation system provides several components: (1) a compiler, (2) a collection of support code for use server-side, (3) a collection of code needed to perform a successful transpilation from Java to Javascript (including emulation of the many of the JDK classes) and (4) a collection of code that can be used to build functional UI's. 

For our purposes we only need (1) and (3). GWT is generally known not for these but for (4) which constitutes a UI system much like JUI (or React, VueJS, etc). This we don't need at all (some of the code is useful, however the GWT project has extracted these into a suite of general-use, GWT and J2CL compatible, third party libraries which we make use of as needed).

The compiler is bundled into the library `gwt-dev.jar` which is referenced from the **jui-stack** parent pom in the *provided* scope as it is only used for compilation (the IDE, codeserver and maven compiler plugin). This is not bundled into application during runtime (and is not needed).

The code for (3) and (4) is bundled into the library `gwt-user.jar`. The latter makes up most of the library and is not useful for our purposes (in fact it is quite incompatible). So rather than make use of it via the library we have brought the relevant source code into this project and this resides under the `com.google.gwt` package (both source and resource). In particular this consists of:

1. Code that is needed to interact with the compiler (mainly oriented around the `GWT` class).
2. Code that is needed for rebinding.
3. Code needed for linking.
4. JDK emulation code (mostly resident under resources).
5. Code that does not have a suitable generalised replacement (limited to `i18n`).

With the exception of the emulation code (which is needed in all cases) it is expected that this code base will be whittled away.

For now, then, we maintain a depedency only on `gwt-dev.jar` for compilation and some of the independent and decoupled utility libraries from the GWT project.

### Upgrading

The code should be upgraded each time there is a GWT release (see the [gwtproject/gwt](https://github.com/gwtproject/gwt) project). This involves:

1. Updating the GWT version in the parent POM to make use of any new compiler features and bug fixes.
2. Updating the code copied over to avoid a dependency on `gwt-user`, thereby taking advantage of bug fixes and wider scope of emulation.

With respect to (2) the following code has been coped directly over from the GWT project:

1. `src/main/resources/com/google/gwt/core` comes from `user/super/com/google/gwt/core`.
2. `src/main/resources/com/google/gwt/emul` comes from `user/super/com/google/gwt/emul`.
3. `src/main/java/javaemul` comes from `user/super/com/google/gwt/emul/javaemul` (this code is duplicated to bring into into compilation scope).
4. `src/main/java/com/google/gwt` comes from `user/src/com/google/gwt` (but only a handful of classes have been copied across, enough to ensure that compilation, rebinding, linking and some features such as code-splitting continue to work, so take care here what to upgrade).

As you can see the most relevant code to maintain parity with will be related to the JDK emulation.

The simplest approach to this is:

1. Clone the [gwtproject/gwt](https://github.com/gwtproject/gwt) into the `tmp` directory of this project module (this is ignored by Git).
2. Checkout the lastest version and the prior version (that was last updgraded to).
3. Run a `git diff release/XXXX release/YYYY --name-only` to work out what files have changed. The important changes will by under `user` (and used the guide above). You can pipe this to filter on package prefix ` | grep '^package/'` (i.e. ` | grep '^user/super/com/google/gwt/emul/'`)
4. Copy across the relevant changes.

In addition you should ensure that the [Modifications: Emulation](#emulation) changes are also applied, unless they have been addressed otherwise (i.e. fixed or updated from the version of GWT being upgraded to).

### Modifications

#### Emulation

The following has been added to `Character`:

```java
public static boolean isSpaceChar(char c) {
    return isSpace(c);
}
```

This has not been implemented as part of the standard emulation (stated reason being the need for a suitable unicode mapping). It is needed for `CharacterValidationRule` in **jui-validation** and the version supplied meets these specific needs.

#### Scheduler

It appeared that invoking `Scheduler.scheduleFinally(...)` outside of the browser event loop would result in the command only being run on the next event (one than is captured, such as a mouse move). The effect of this was that some script injections were being delayed resulting in initially no CSS being applied. To resolve `Impl.running()` was added to determine if the entry was in process. `Scheduler.scheduleFinally(ScheduledCommand)` was modified to check if we are in enter and if not then to execute the command immediately.

### Pending decoupling work

The following activities are pending decoupling from GWT:

1. CSS resources
2. i18n resources
3. Style is still being used
4. Window is still being used
5. com.google.gwt code minimisation
6. Minimisation of module configuration

### GWT compilation process

This is a very rought outline of the compilation process. Some additional, and excellent, resources include the Google [presentation](https://docs.google.com/presentation/d/1n0BSQGCBkxfHLzDVFCMyWjqTYuraUr09uc7n5n6JuLU/) and the article [The GWT Toolkit: Build Powerful JavaScript Front Ends Using Java](https://www.toptal.com/front-end/javascript-front-ends-in-java-with-gwt).

#### Precompilation

1. Constraints of compilation are extablished (via arguments and determination of the scope of source code as determined by the module hierarachy and the respective `source` tags). This included initialisaing the compilation cache (for incremental this may be an existing one).
2. Permutations are calculated (these are associated with declared browser types and languages, the former we don't use anymore but the latter is used to package language specific resources and concerns) and rebinding performed.
3. Entry points are determined and code is parsed into a Java AST from there.
4. Adjustments are performed (i.e ReplaceRunAsyc, etc), AST is optimised and rebind points recorded.

#### Compilation

The Java AST is translated to a JavaScript AST from which JavaScript is generated (including code splitting). The Java AST structure resides under `com.google.gwt.dev.jjs.ast` and is fairly standard.

From this a special mirror structure is formed (the `JTypeOracle`) upon which optimisations are performed:

1. Pruner (removes unreachable code).
2. Finalizer (marks relevant code as final).
3. MakeCallsStatic (making monomorphic instance method calls static).
4. TypeTightener (compute more precise types).
5. MethodCallTightener (calculated more precise overrides).
6. DeadCodeElimination (constant folding - replacing `2+5` with `7`, static evaluation, etc).
7. MethodInliner (inline small method).
8. EnumOrdinalizer (replace enums, where feasible, with ordinals).
9. SameParamaterValueOptimizer (internalises parameters where same value is used in all instances).

The generalised optimsation flow (for each optimisation) is two pass: first pass using `JVisitor` for collecting information and second pass with `JModVisitor` to apply the optimisation.

Following this normalisations are performed:

1. Devirtualizer (ie, devirtualiser overlay object)
2. CatchBlockNormalizer (collapses multiple catches to sequential `if` statements).
3. LongCastNormalizer (inserts explicit casts when `long` is involved).
4. LongEmulationNormalizer (replaces `Long` with the emulation `LongLib`).
5. ImplementCastsAndTypeChecks (replaces `instanceof` with calls to the `Cast` class).
6. ArrayNormalizer (replaces array operations with corresponding runtime calls).
7. EqualityNormalizer (replaces `==` with the appropriate JavaScript version).
8. ReplaceGetClassOverrides (replaces `getClass()` with `this.__clazz`).
9. ResolveRuntimeTypeReferences (replaces references to types as appropriate).

Now conversion to a JavaScript AST is performed, involves multiple passes (including fixing name clashes, defining scopes, etc). Conversion follows a specific layout which encompasses various standard element (i.e. `this.__clazz`). This AST then undergoes further optimisation and normalisation. From here the JavaScript is generated.

#### Linking

Here the products of compilation are generated, involved are three types of linker:

1. Pre-linker that produces artefacts that are seen by the primary linker(s).
2. Primary-linker that process the JS output and pre-linker outputs (i.e. generates the `.nocache.js` bootstrapper, `.cache.js` JS file and `deferred/<n>.js` deferred JS files).
3. Post-linker that generates the final artefacts for packaging, including supporting artefacts such as source maps.