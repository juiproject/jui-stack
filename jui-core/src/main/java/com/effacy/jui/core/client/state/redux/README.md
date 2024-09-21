# Overview

**This is experimental, it is not ready for production.**

This provides an implemention of the principles described in [Redux](https://redux.js.org/) with the following considerations:

1. A state store is separated into a `IStateMutator` (for which actions can be dispatched to) and a `IStateStore` (that extends `IStateMutator` and can have subscribers registered against).
2. The default implementation of `IStateMutator` is `StateMutator` that provides a store of the current state, assignment of an initial state and the registration of reducers.
3. The default implementation of `IStateStore` is `StateStore` and includes an `IStateMutator` instance which it delegates to (this can be passed during construction or a default will be created on construction).
4. Actions are passed through via the `Action` class that includes a string based type and optional payload:
    4.1 An optional failure consumer can be added that accepts an `IFailureReason` and can be used to respond to a failed action.
    4.2 The type can be a path expressed either as an array or as a slash separated sequence. Paths must be appropriately resolved within a `IStateMutator`.
    4.3 The payload is un-typed and needs to be cast at the reducer (so proper error-handling should be in place).
5. One can register mutators against a static store using `Redux.register(...)`. Registration is against a specified path and that path must be used as a prefix to actions directed at the mutator.
6. One can dispatch actions to `Redux` registererd mutators via `Redux.dispatch(...)`. Again the path of the action must include the path used to register the target mutator as a prefix to the type.
7. States can implement `State` and can provide their own reducers (actually not declared as reducers but simply able to respond to actions).
8. Dispatching an action will result in a returned outcome which is one of `Outcome.SUCCESS` (processed successfully), `Outcome.REJECT` (no reducer found to process the action) and `Outcome.FAILURE` (was processed but was subject to a problem). In the latter case any failure handler on the action will have been invoked.

# Documentation

Usage documentation is found in the `jui-playground` documentation hub.

# Design

## Asynchronous calls



# Testing

Unit tests reside in the `com.effacy.jui.core.client.state.redux` package of the test source sub-tree.