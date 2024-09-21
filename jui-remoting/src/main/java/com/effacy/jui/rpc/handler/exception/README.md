# Overview

There are two core exception to the handler framework: `NoProcessorException` (where a processor has not been mapped to the incoming query or command) and `ProcessorException` (which arises when an error state occurs internal to processing a query or command).

Note that the former exception is invoked only within the handler framework itself (i.e. from the internal processor registries). The latter arises from within the implementations of processor and that will be elaborated on in the following.

# Processor exceptions

As noted `ProcessorException` is the exception that is expected to arise from the processing implementation when something goes amiss.  This exception bundles a collection of `ProcessorException.Error`'s that encapsulate the various error conditions (from blocking errors such as an entity not being found through to a multiplicity of error such as may be the case where more than one field does not pass validation).

Each `ProcessorException.Error` captures a mandatory type (`ProcessorException.ErrorType`) and optional target, code and message.  The target is intended to aid in localising the error to a specific context (for example, a specific field) while the code and message carry the detail of the problem.  The actual use of these fields is at the discretion of the implementation.

A special note on the target is that it can be scoped into a chain of localisations.  This is particularly pertinent when there are multiple or nested commands that need to be disambiguated.  The convention is that scoping is represented as a list of scopes terminating in the original target whether the list is represented as a string with items separate by a forward slash.

To support and standardise some of the more common error scenarios there are a number of derived exceptions (such as `NotFoundProcessorException`) that extend `ProcessorException` and that put in place an error (or errors) in a useful manner (and tend to align with the `ProcessorException.Error`).