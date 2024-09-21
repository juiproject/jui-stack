# Overview

This package contains the various builders used by the code server to package, format and deliver various resources.

Each of these builders extends `ViewBuilder` and is constructed with the relevant data to generate the view. The `ViewBuilder` itself a functional interface that (in effect) maps an `HttpServletRequest` and its internal data to a `HttpServletResponse`, thereby generating content to be sent back to the browser.

Each of the classes is documented individually as to its intended purpose.