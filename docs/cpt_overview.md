# Components & controls

JUI provides a standard suite of components and controls which are found in the **jui-ui** library. Controls are quite specialised and find themselves in a dedicated package `com.effacy.jui.ui.client.control` while non-control components tend to reside under their own packages.

These are intended only to augment your own components, or to provide a basis to build your own variants. When it comes to controls, you will more likely use the off-the-shelf variants. However you are in no way obliged to do so and you can relatively easily create your own (see [Controls: Custom controls](ess_controls.md#custom-controls) on how to build a control). In all cases you are encouraged to use these a guides.

Most of the components and controls are fairly self-explanatory though we describe some of the more involved. In the latter case we provide more detailed desrcriptions in this documentation as follows:

1. [Controls helper](#controls-helper) introduces a helper class that can be used to create any of the standard JUI controls (which acts more of a reminder system than anything else).
2. [Recipies](#recipies) for examples and tenmplates.
3. [Component descriptions](#component-descriptions) for references to details description of the more involved components (and controls).

## Controls helper

All controls follow the standard pattern of configuration and creator (as per components). As a convenience `Controls` contains static creator method for creating controls in a single place:

```java
TextControl ctl = Controls.text (cfg -> {
    cfg.placeholder ("Full name");
    cfg.validator (
        Validators.notEmpty ("please provide the persons name"),
        Validators.length (0, 100, "cannot be more than 100 character")
    );
});
```

Note that `Validators` serves a similar function for the standard validators from `jui-validation` (in the `com.effacy.jui.validation.model.validator` package).

The above is equivalent to the standard approach (though with the above one only needs to remember `Controls`):

```java
TextControl ctl = TextControlCreator.build (cfg -> {
    cfg.placeholder ("Full name");
    cfg.validator (
        Validators.notEmpty ("please provide the persons name"),
        Validators.length (0, 100, "cannot be more than 100 character")
    );
});
```

Controls, as with components in general, can be inserted directly into rendering blocks:

```java
private TextControl ctl;

protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (root -> {
        Div.$ (root).$ (field -> {
            Label.$ (field).text ("Persons name");
            ctl = TextControlCreator.$ (field, cfg -> {
                cfg.placeholder ("Full name");
                cfg.validator (
                    Validators.notEmpty ("please provide the persons name"),
                    Validators.length (0, 100, "cannot be more than 100 character")
                );
                cfg.modifiedHandler ((ctl,val,prior) -> {
                    // val holds the updated value, prior the prior value.
                    Logger.log (val);
                });
            });
        });
    }).build ();
}
```

The employment of controls as described above is ideal when being used outside of the context of form-based data capture or modification(for example, when implementing a search box or search filter). When it comes to forms we need to properly handle errors as well as potentially managing conditional state (i.e. the hiding or disabling of controls and collections of controls). You can either implement your own approach or make use of JUI's out-of-the-box `ControlForm` mechanism described in the following section.

## Recipies

It is sometimes quite useful to have quite explicit examples at hand that can be used as a guide, or simply to copy-and-paste then modify. A collection of such [Recipies](cpt_recipies.md) is provided based on commonly encountered scenarios and employed design patterns.

## Component descriptions

Most of the standad JUI components and controls are fairly straightforward, however some can be served better with a more detailed description that what can be found in the Javadoc (or supporting documentation, lessons and examples). We provide detailed descriptions of:

1. [Selection control](cpt_selection.md) for the selection of a fixed or dynamic collection of records.
2. [File upload control](cpt_uploader.md) for the upload of files.