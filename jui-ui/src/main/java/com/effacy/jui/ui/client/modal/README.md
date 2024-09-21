## Overview

A modal dialog is effectively a reusable floating container that encapsulates a component.  Such a container can be adorned with additional presentation (i.e. border, title bar) and functionality (close action) and can be used either in a one-off or on-going fashion.

Structurally the base of all dialogs is `Modal`.  This implements the most basic level of behaviour with the most basic of presentation (just enough to contain a component and outline the bounding area).  There are different presentation models that can be selected (for example, a traditional central window or a sliding panel).  A more feature rich version is `ModalDialog` that provides the facility to render a title structure, close action and actions that appear at the footer of the presentation region.

There is also a more general need to present confirmations, alert and other simple dialogs.  To support this there is `NotificationDialog` which offers a number of utility methods for various cases.  This is not intended to be extended.