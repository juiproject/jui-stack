# Lesson 4

**By the end of this lesson, your will know how to use controls, how to compose controls into forms and how to move data in and out of those forms and be able to create basic custom controls.**

?>It is assumed you have read through the [code structure](lessons.md#code-structure) documentation in [Lessons](lessons.md) that describes where the sample code resides and how CSS styles are treated in the examples. If you have not already worked through [lesson 1](lessons_1.md) your are encouraged to do so before embarking on this lesson. Additional information that is relevant to this lesson is contained in [Components](ess_components.md) and [Controls](ess_controls.md).

Controls are an essential element of any interactive application, and JUI applications are no exception. Controls are themselves components however they contract to a specific set of behaviour that relate to the displaying of and manipulation of some value. This lesson explores components, how to compose those components into form and how to work with data:

1. [Part A](#part-a-controls) we introduce controls, their states and their behaviours.
2. [Part B](#part-b-forms) we discuss forms as a means to set out controls.
3. [Part C](#part-c-handling-data) we discuss moving data in and out of forms and dealing with remote errors.
4. [Part D](#part-d-custom-controls) we introduce custom controls.

*Recall that working solutions can be found in the classes `Lesson4a`, `Lesson4b`, etc in the `com.effacy.jui.playground.ui.lessons` package. Most of the solutions are given as inner classes of the lesson part, these are referenced in bold at the begining of each subsection.*

## Part A: Controls

Here we cover a simple introduction to controls (using the standard JUI controls as exemplars).

### Creating controls

**Refer to `VariousControls`**

Controls are just components and are created in the same manner. However, for the JUI standard controls the convenience class `Controls` is provided that declares creator methods for each of the standard control types. This simply provides a catalogue of the controls in one place saving having to remember the relevant control class to use.

The creator methods (generally) take a lambda-expression which is used to configure the control:

```java
// This creator method is passed a lambda-expression that enables one to
// configure the control.
Controls.text (cfg -> {
    cfg.width (Length.px (150));
})
```

Putting these concepts together we can create a component that displays multiple controls:

```java
public class VariousControls extends SimpleComponent {

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "stack", "gap");
            H3.$ (root).text ("Exploring different types of control");
            Div.$ (root).$ (
                Label.$ ().text ("Text control"),
                Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                })
            );
            // Just to mix it up a bit we build this DIV using the
            // lamba-expression approacg, hence the Cpt.$ (...) to
            // insert the component.
            Div.$ (root).$ (item -> {
                Label.$ (item).text ("Text area control");
                Cpt.$ (item, Controls.textarea (cfg -> {
                    cfg.width (Length.px (150));
                }));
            });
            Div.$ (root).$ (
                Label.$ ().text ("Selection"),
                Controls.selector (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.allowEmpty ();
                }, "Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 6", "Option 7")
            );
            Div.$ (root).$ (
                Label.$ ().text ("Checkbox"),
                Controls.check (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.label ("Option to activate");
                })
            );
            Div.$ (root).$ (
                Label.$ ().text ("Option group"),
                Controls.checkGroup (cfg -> {
                    cfg.width (Length.px (250));
                    cfg.option("option1", null, "The first option", "A description for the first option");
                    cfg.option("option2", null, "The second option", "A description for the second option");
                })
            );
            Div.$ (root).$ (
                Label.$ ().text ("Multi-check"),
                Controls.checkMulti (cfg -> {
                    cfg.width (Length.px (250));
                    cfg.option("option1", "Option 1");
                    cfg.option("option2", "Option 2");
                    cfg.option("option3", "Option 3");
                })
            );
            Div.$ (root).$ (
                Label.$ ().text ("Radio group"),
                Controls.radioGroup (cfg -> {
                    cfg.width (Length.px (250));
                    cfg.option("option1", "Option 1", "A description for the first option");
                    cfg.option("option2", "Option 2", "A description for the second option");
                })
            );
        }).build();
    }
}
```

which can be added into the constructor of `Lesson4a` with:

```java
add (new VariousControls ());
```

You are encouraged to look through the various creator methods in `Controls` to see what sort of controls are available. Implentations for standard JUI controls can be found in `com.effacy.jui.ui.client.control`.

### Control values

**Refer to `ControlValues` and `ControlValuesWithModification`**

All controls implement `IControl<V>` which contracts them to declare a value of a specified type `V` (i.e. `TextControl` is of type `String`) which can be assigned and retrieved using one of the `setValue(...)` methods (for assignment) and `V value()` (for retrieval).

The following example presents a single text control that the user can enter a name into then a button the retrieves that name to display in a dialog then clears the name for the next round of processing.

```java
public class ControlValues extends SimpleComponent {

    private TextControl nameCtl;

    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "panel");
            P.$ (root).text ("Exploring control values");
            Div.$ (ctls).$ (
                Label.$ ().text ("Persons name"),
                nameCtl = Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                })
            );
            Div.$ (ctls).$ (item -> {
                ButtonCreator.$ (item, cfg -> {
                    cfg.label ("Process");
                    cfg.handler(() -> {
                        DomGlobal.alert ("Name is \"" + nameCtl.value () + "\"");
                        nameCtl.setValue ("");
                    });
                });
            });
        }).build();
    }
}
```

To clear the value on the control we simply set it to the empty string with `nameCtl.setValue ("")`. Here we are using the variant of `setValue(V)` that takes the raw value we are wanting to assign. There is a variant of this that takes an instance of `Value` which wraps the raw value we are wanting to assign while allowing us to specify certain behaviours we want the control to abide by when the value is assigned.

The following example illustrates this with a text field and two buttons. The text field has a modification listener attached to it (this is done by assiging a `modifiedHandler(...)` during configuration of the control). One of the buttons assign a uniue value (a UID in this case) with the raw assignment approach while the second wraps the value in a `Value` instances and specifies that the update be *quiet* (no modification event is generated).

```java
public class ControlValuesWithModification extends SimpleComponent {

    private TextControl nameCtl;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "panel");
            P.$ (root).text ("Exploring control value assignment");
            Div.$ (root).$ (ctls -> {
                Div.$ (ctls).$ (
                    Label.$ ().text ("Random value"),
                    nameCtl = Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler ((ctx, val, prior) -> {
                            DomGlobal.alert("Value is \"" + nameCtl.value () + "\"");
                        });
                        // We also make the field readonly, so the only way to change
                        // it is via the buttons.
                        cfg.readOnly();
                    })
                );
                Div.$ (ctls).$ (item -> {
                    ButtonCreator.$ (item, cfg -> {
                        cfg.label ("Set value");
                        cfg.handler (() -> {
                            nameCtl.setValue (UID.createUID ());
                        });
                    });
                });
                Div.$ (ctls).$ (item -> {
                    ButtonCreator.$ (item, cfg -> {
                        cfg.label ("Set value quietly");
                        cfg.handler(() -> {
                            nameCtl.setValue (Value.of (UID.createUID ()).quiet ());
                        });
                    });
                });
            });
        }).build();
    }
    
}
```

You will observe that when the first button is clicked the text control changes its value and a dialog is shown. When the second button is clicked the text control changes its value, but no dialog is show (since no modification event is generated).

### Control states

**Refer to `ControlStates`**

Controls support a variety of states that impact behaviour and presentation. Here we explore the following states:

1. **Normal** where the control is receptive to user input.
2. **Disabled** where the control does not interact with the user (generally this is a temporary state such as when the control is only relevant in the context of the setting of another control).
3. **Read-only** where the control's only purpose is to present information in keeping with the aesthetic of its surrounding controls and does not interact with the user (generally this is not a temporary arrangement but reflects some other imposition such as a lack of privileges).
4. **Waiting** where the control has no valid information to display pending provisioning of the information (i.e. when data is being loaded remotely).
5. **Error** where the control indicates an error condition on the information it holds (i.e. what the user has entered is not valid).

The following creates a simple component that presents text control in each of the states above:

```java
public class ControlStates extends SimpleComponent {

    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "panel");
            P.$ (root).text ("Exploring controls in different states");
            Div.$ (root).$ (ctls -> {
                Div.$ (ctls).$ (
                    Label.$ ().text ("Normal"),
                    Controls.text (cfg -> {
                        cfg.placeholder ("First name");
                        cfg.width (Length.px (150));
                    })
                );
                Div.$ (ctls).$ (
                    Label.$ ().text ("Disabled"),
                    Controls.text (cfg -> {
                        cfg.placeholder ("First name");
                        cfg.width (Length.px (150));
                    }, ctl -> {
                        // See the note below which explains how we can
                        // act on the control instance itself to set
                        // state.
                        ctl.disable ();
                    })
                );
                Div.$ (ctls).$ (
                    Label.$ ().text ("Read only"),
                    Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                    }, ctl -> {
                        ctl.readOnly (true);
                        ctl.setValue ("Jill Jones");
                    })
                );
                Div.$ (ctls).$ (
                    Label.$ ().text ("Waiting"),
                    Controls.text (cfg -> {
                        cfg.placeholder ("Jill Jones");
                        cfg.width (Length.px (150));
                    }, ctl -> ctl.waiting (true))
                );
                Div.$ (ctls).$ (
                    Label.$ ().text ("Error"),
                    Controls.text (cfg -> {
                        cfg.placeholder ("Jill Jones");
                        cfg.width (Length.px (150));
                    }, ctl -> ctl.invalidator ().invalidate ())
                );
            });
        }).build();
    }
}
```

We note that we assign the state not via configuration but on the control instance itself. The creator methods on `Controls` faciliate this by allowing one to pass a second lambda-expression that gives access to the control instance:

```java
Controls.text (cfg -> {
    // Here we apply configuration to cfg.
    cfg.placeholder ("First name");
    cfg.width (Length.px (150));
}, ctl -> {
    // Here we can act on the control instance
    // itself as ctl.
    ctl.disable ();
})
```

Some states can be assigned through configuration (i.e. `readOnly`) but generally state transitions occur during runtime so only make sense through the instance.

### Dynamic controls

**Refer to `DynamicControls`**

One can use a controls' value to change the state of another control by combining the techniques described above.

In the following example we create two controls to represent the selection of a person's title. We allow a custom title to be provided as an alternative to the given fixed titles. We only want the text control that allows the user to enter an alternative title to be activated when the *Other* option has been selected, this we can do using a modification handler.

```java
public class DynamicControls extends SimpleComponent {

    private TextControl otherTitleCtl;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "panel");
            P.$ (root).text ("Explores enabling and disabling a control based on another controls value");
            Div.$ (root).$ (ctls -> {
                Div.$ (ctls).$ (
                    Label.$ ().text ("Title"),
                    Controls.selector (cfg -> {
                        cfg.allowSearch (false);
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler((ctl, val, prior) -> {
                            if ("Other".equals (val)) {
                                otherTitleCtl.enable ();
                            } else {
                                otherTitleCtl.setValue ("");
                                otherTitleCtl.disable ();
                            }
                        });
                    }, "Mr", "Mrs", "Ms", "Miss", "Mx", "Master", "Other")
                );
                Div.$ (ctls).$ (
                    Label.$ ().text ("Other title"),
                    otherTitleCtl = Controls.text (cfg -> {
                        cfg.placeholder ("Other title");
                        cfg.width (Length.px (250));
                    }, ctl -> ctl.disable ())
                );
            });
        }).build();
    }
    
}
```

### Search and filtering

**Refer to `SearchableTable`**

One common use for controls outside of data capture is for search and filter. This is illustrated in the following example where we employ a text control to provides a simple keyword search into a table of items.

```java
public class SearchableTable extends SimpleComponent {

    private SampleRecordStore store = new SampleRecordStore ();
    {
        store.load (10);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "stack");
            P.$ (root).text ("Controls for search");
            Cpt.$ (root, Controls.text(cfg -> {
                cfg.placeholder ("Search table");
                cfg.iconLeft (FontAwesome.search ());
                cfg.clearAction ();
                cfg.width (Length.px (200));
                cfg.modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                    if (StringSupport.empty (val))
                        store.filter (null);
                    else
                        store.filter (r -> 
                            r.name.toLowerCase ().contains (val.toLowerCase ()) ||
                            r.description.toLowerCase ().contains (val.toLowerCase ())
                        );
                }));
            }));
            Div.$ (root).$ (container -> {
                container.css (CSS.HEIGHT, Length.px (300)).css (CSS.WIDTH, Length.px (500)).css (CSS.MARGIN_BOTTOM, Length.em (2));
                TableCreator.$ (container, cfg -> {
                    cfg.header ("Name", header -> {
                        header.renderer (TextTableCellRenderer.create (r -> r.name));
                        header.width (Length.em (5));
                    }).header ("Description", header -> {
                        header.renderer (TextTableCellRenderer.create (r -> r.description));
                        header.width (Length.em (10));
                    });
                }, store);
            });
        }).build ();
    }

    public class SampleRecord {

        private String name;

        private String description;

        public SampleRecord(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public class SampleRecordStore extends ListPaginatedStore<SampleRecord> {

        @Override
        protected void populate(List<SampleRecord> records) {
            for (int i = 0; i < 100; i++)
                records.add (new SampleRecord (Names.NAMES[i], "Description " + i));
        }

    }

}
```

For this example we make use of a table (tables are explored in [Lesson 6](lessons_6.md), though for this demonstration the specific details are not important) backed by a data store consisting of a collection of records, each with a randomly selected name (from `Names` which is support class for prototyping) and an arbitrary (but unique) description. The store can be filtered and it is this feature that we make use of.

The search is implemented by repsonding to changes in the search control and passing these through as a filter to the store (the store provides a `filter(Predicate)` method to provide for context-specific filtering). In this case we employ a simple sub-string match within either the name or the description (and the clearing of the filter when there is no search content).

Of note is the use of `DelayedModifiedHandler` creates a delegating modification handler that incurs a delay. The reasoning behind this is that we suppress rapid changes promoting only the last after the succession of changes has ceased (roughly). This is particularly useful when the store performs remote queries and we are not wanting to make remote invocations on every key press.

### Validation

The final topic for this part is validation. Validators can be assigned to any control during configuration and are processed whenever the control's `validate()` method is invoked. If any of the validators fail two things happen:

1. the control is placed in an *invalid* state (generally accompanied by a change in presentation to indicate the control is invalid), and
2. an `IInvalidListener.onInvalidated(...)` event is emitted (for details on events see [Events](ess_events.md)).

If the validation passes then any prior invalidation state is cleared (and the `IInvalidListener.onClearInvalidated(...)` event is emitted).

The following illustrates the validation mechanism.

```java
public class ControlValidation extends SimpleComponent {

    private TextControl nameCtl;

    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4", "panel");
            H3.$ (root).text ("Explores enabling and disabling a control based on another controls value");
            Div.$ (root).$ (ctls -> {
                Div.$ (ctls).$ (
                    Label.$ ().text ("Person's name"),
                    nameCtl = Controls.text (cfg -> {
                        cfg.placeholder ("Full name");
                        cfg.width (Length.px (250));
                        cfg.validator(
                            Validators.notEmpty ("Cannot be empty"),
                            Validators.length (0, 20, "Cannot be more than 20 characters")
                        );
                    }, ctl -> {
                        ctl.addListener(IInvalidListener.create((c, errors) -> {
                            Logger.log ("Control is invalid:");
                            errors.forEach (error -> Logger.log (" " + error));
                        }, null));
                    })
                );
                ButtonCreator.$ (ctls, cfg -> {
                    cfg.label ("Validate");
                    cfg.handler (() -> {
                        nameCtl.validate ();
                    });
                });
            });
        }).build();
    }
    
}
```

Here we declare two validators, one for being empty and the other for length. An event listener is also attached to write to the conole the error messages. A button is used to invoke the controls validate method. When we do so (and assuming the text controls content is invalid) we only see the control changing to its invalid state and we do not see any error messages. Controls themselves do not present errors but rather leave that to the content into which they have been deployed. In our case we don't have any explicit mechanism to present the actual error messages and this leads to our next part where we discuss *forms*.

### Exercises

Building on what you have learnt in this part try doing the following exercises:

1. You have used the creator methods on `Controls` to create the various controls. How would you create a control without using `Controls`? 
2. Create a component that displays two text controls such that when you update one the other reflects the changes you make (here make sure you set values quietly, as in the last example of [Control values](#control-values)). Use the render in constructor approach (see [Lesson 1: Render in constructor](lessons_1.md#render-in-constructor)). Do you need to do the update quietly?
3. Modify exercise (2) so that you use a delayed modifier (similar to what was used in [Search and filtering](#search-and-filtering)). What do you notice about the effect of the delay?
4. The `TextControl` has the configuration option `expandOnFocus(Length)` that will expand the height of the control when it gains focus (to the specified length) and revert back to the default height when it looses focus. Assume this option was not available but you wanted the control to behave that way; how could you achieve that? (assume that setting the height of the control to `null` reverts to the default height).

Candidate solutions are outlined in [Solutions to the exercises](#solutions-to-exercises).

## Part B: Forms

We now move onto forms as a means to layout and manage collections of controls specifically for data capture. All code is contained in `Lesson4b`.

### Simple form

**Refer to `FormA`**

JUI provides a standard mechanism for constructing forms and one that supports a range of form-related capabilities embodied the class `ControlForm`. *Not that this is not a requirement for developing forms and you are welcome to develop your own form mechanisms as applicable to the needs of your application.*

The core structure of `ControlForm` lies in the *group* (an instance of `GroupBuilder` being an inner class of `ControlForm`) which allows one to build a heirarchy of regions that ultimately setout the controls. These regions allow for the control of spacing, the display of header and footer information and for providing a means to collective manage a logically grouping of controls. We will explore these features in more detail later in the lesson.

The controls that reside in a group are arranaged in *rows* with each row containing one or more *cells*. Each cell can contain a single component or control along with a supporting label. The cell also provides the mechanism to present error messages as generated by the component.

To illustrate these concepts we start with a fairly simple from that allows for the capture of a persons name:

```java
public class FormA extends ControlForm<Void,Void> {
        
    public FormA() {
        super (ControlFormCreator.create ().maxWidth (Length.px (600)));

        group (grp -> {
            grp.row (row -> {
                row.control ("firstName", "First name", Controls.text (cfg -> {
                    cfg.placeholder ("First name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                });
                row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                    cfg.placeholder ("Middle name(s)");
                    cfg.validator (
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1);
                });
                row.control ("lastName", "Last name", Controls.text (cfg -> {
                    cfg.placeholder ("Last name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                });
            });
        });
        bar (bar -> {
            bar.add (ButtonCreator.build (cfg -> {
                cfg.label ("clear").style (Button.Config.Style.LINK);
                cfg.handler (() -> {
                    FormA.this.reset ();
                });
            }));
            bar.add (ButtonCreator.build (cfg -> {
                cfg.label ("Create");
                cfg.handler (() -> {
                    if (FormA.this.validate())
                        DomGlobal.alert ("Success!");
                });
            }));
        });
    }
}
```

The class extends `ControlForm` directly (we will delve into the parameterisation later, but for now we leave these as `Void`) with the constructor building out the form elements. First we create a single group and within that group we create a single row. Within the row we create three controls (each in their own cells) using the `control(String name, String label, IControl control, Consumer<IControlCell> cell)` method whose arguments are:

1. `name` being a reference to the control used for lookups (this is optional),
2. `label` being the display label to present,
3. `control` being the control to add, and
4. `cell` being an optional consumer that can configure the cell.

The three controls capture the various name parts (first, middle and last) with validators that limit length and require the first and last names (the validators are created using the `Validators` class as a convenience, much in the way that `Controls` has been employed). We now look closely at the `IControlCell`, in particular:

1. `grow(1)` tells the cell to expand out and fill any space left in the row (if more than one cell has a grow assignment then they are proportionally expanded). *Try removing this and observing the change.*
2. `required()` marks the control as being required. It is important to not that this does **not** perform any validation on the control but **only** affects the presentation (for the default style this places a star to the left of the label and boldens the label).

?> Try removing the `grow(1)` as observe what changes, try changing the argument to `grow(...)` and observe the effect.

We also create a *button bar* (with the `bar(Consumer<Panel>)` method) and place in it two buttons. The first button invokes the forms `reset()` method which resets all the controls managed by the form which inturn clears any invalidation. The second button mimics a form submission but first validating the form and if validation passes displays a success message.

If you submit the form without valid data you should see the errant controls being marked in red and the associated error messages appearing under the associated control. *Try adding additional validators and changing the messages to see the effects.*

### Dynamic forms

**Refer to `FormB`**

In the [Part A](#part-a) we introduced a pair of controls for assigning a persons title, allowing for a custom title. We incorporate that into the example above by adding the following as a second row:

```java
...
grp.row (row -> {
    row.control ("title", "Title", Controls.selector (cfg -> {
        cfg.allowSearch (false);
        cfg.width (Length.px (150));
        cfg.validator (
            Validators.notEmpty ("Cannot be empty")
        );
    }, "Mr", "Mrs", "Ms", "Miss", "Mx", "Master", "Other"), cell -> {
        cell.required ();
        cell.modify ((ctx,ctl) -> {
            if ("Other".equals (ctl.value  ()))
                ctx.enable ("titleOther");
            else
                ctx.disable ("titleOther");
        });
    });
    row.control ("titleOther", "Other", Controls.text (cfg -> {
        cfg.placeholder ("Other title");
        cfg.validator (
            Validators.notEmpty ("Cannot be empty")
        );
    }), cell -> {
        cell.disable ().grow (1).required ();
    });
});
...
```

We note the similarity with the key change being that we have removed the modification handler associated directly with the selector control and replaced it with a handler declared against the cell handler:

```java
cell.modify ((ctx,ctl) -> {
    if ("Other".equals (ctl.value  ()))
        ctx.enable ("titleOther");
    else
        ctx.disable ("titleOther");
});
```

The `modify(IModification)` method is passed a lambda expression that is passed a `IModificationContext` (the `ctx`) and the control itseld (the `ctl`). In this specific example we interrogate the control and if the value is `Other` we enable the control for the user to enter the custom title, otherwise we disable it. We could do this by first retaining a direct reference to the control (as was done in [Part A](#part-a)) but in this case we make use of an indirect reference using the name declared when creating the control (`titleOther`) and a lookup from the modification context (this is done via the `enable(...)` and `disable(...)` methods that combine lookup with a component enable or disable). *This is not type safe so is a matter of preference whether you choose to take the indirect reference approach or the direct one.*

### Supporting commentary

**Refer to `FormC`**

The control form allows one to specify, for each group, a *header* and a *footer*. We extend on the above by adding a title with instruction in the header and some guidance text in the footer:

```java
grp.header (header -> {
    header.title ("Name and salutation");
    header.instruction ("Provide the persons name and their salutation.");
});
grp.footer(footer -> {
    footer.guidance ("Note that the title will be used in all communications with the person.");
});
```

We can also provide guidance on a per-cell basis. We make use of this for the custom title:

```java
row.control ("titleOther", "Other", Controls.text (cfg -> {
    cfg.placeholder ("Other title");
    cfg.validator (
        Validators.notEmpty ("Cannot be empty")
    );
}), cell -> {
    cell.disable ().grow (1).required ();
    cell.guidance ("Try to keep this as brief as possible.");
});
```

### Conditional groups

**Refer to `FormD`**

Groups can also be *conditional* which allows the user to active a group and expose the controls within. We extend our form by additing a new top-level group that asks for "additional entitlements". Each entitlement requires a justification so we use a sub-group for each, each of which is conditional (one for "extra funding" and another for "accommodation and travel"). These groups are marked as being *conditional* which means they appear with a checkbox next to their title. Checking this box will display the contents of the sub-group.

```java
group (grp -> {
    grp.header (header -> {
        header.title ("Additional entitlements");
        header.instruction ("Record additional entitlements the person may be eligible for.");
    });
    grp.group (funding -> {
        funding.by ("entitlementFunding");
        funding.conditional (conditional -> {});
        funding.header(header -> {
            header.title ("Extra funding");
            header.instruction("Covers funding not related to accommodation and travel.");
        });
        funding.control ("entitlementFundingJustification", "Justification", Controls.textarea (cfg -> {
            cfg.rows (3);
            cfg.validator (
                Validators.notEmpty ("Please provide a justification")
            );
        }), cell -> cell.grow (1));
    });
    grp.group (funding -> {
        funding.by ("entitlementAccomodation");
        funding.conditional (conditional -> {});
        funding.header(header -> {
            header.title ("Accommodation and travel");
            header.instruction("Accommidation and travel over 100km away from home.");
        });
        funding.control("entitlementAccomodationJustification", "Justification", Controls.textarea (cfg -> {
            cfg.rows (3);
            cfg.validator (
                Validators.notEmpty ("Please provide a justification")
            );
        }), cell -> cell.grow (1));
    });
});
```

If you action the form then the validators without checking either of the conditional groups then text-areas will not be validated. This is due to the fact that conditional groups will suspend all contained controls (this is an additional state that a control can have that prevents it from participating in validation) until they are opened.

?>Try making the following changes to see their effects: (1) move the second group (with the conditional sub-groups) as a child of the first group rather than being its sibiling, and (2) for each of the conditional groups use the `group(...)` method when configuring the conditions to assign both to the same named *grouping* (this should turn them from checkboxes to radio buttons allowing only one of the groups to be active at any one time).

## Part C: Handling data

Here we explore dealing with data from (and to) forms including remote validation. All code is contained in `Lesson4c`.

In the following we make use of the data type:

```java
public class Name {

    public String first;

    public String middle;

    public String last;
    
}
```

The setters are getters are omited for simplicity (rather we access the members directly).

### Simple create form

**Refer to `CreateForm` and `CreateFormVariant`**

Here we consider a form that is initialy blank and is used to capture data for the subsequent creation (of some entity). The example below captures a persons name.

There are several approaches one may consider to assembling the data from the form for processing:

1. **Setters** where specific setters are assigned for each cell containing a control and these are responsible for extracting the control value and assigning it.
2. **Extraction** where control values are extracted from the form using the supplied references to the controls. *This is not type safe but is convenient.*
3. **Direct** where controls are assigned to class members and values taken directly. This is more cumbersome but is type safe. This is fairly traditional (and straight forward) so is not presented as an example.

We begin with option (1) as illustrated in the following:

```java
// Create person form
public class CreateForm extends ControlForm<Void,Name> {

    private Button updateBtn;
    
    public CreateForm() {
        super (ControlFormCreator.create ().maxWidth (Length.px (600)));

        group (grp -> {
            grp.row (row -> {
                row.control ("firstName", "First name", Controls.text (cfg -> {
                    cfg.placeholder ("First name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                    cell.set ((ctx,v,n) -> n.first = v);
                });
                row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                    cfg.placeholder ("Middle name(s)");
                    cfg.validator (
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1);
                    cell.set ((ctx,v,n) -> n.middle = v);
                });
                row.control ("lastName", "Last name", Controls.text (cfg -> {
                    cfg.placeholder ("Last name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                    cell.set ((ctx,v,n) -> n.last = v);
                });
            });
        });
        bar (bar -> {
            bar.add (ButtonCreator.build (cfg -> {
                cfg.label ("clear").style (Button.Config.Style.LINK);
                cfg.handler (() -> {
                    CreateForm.this.reset ();
                });
            }));
            bar.add (updateBtn = ButtonCreator.build (cfg -> {
                cfg.label ("Create");
                cfg.handler (() -> {
                    if (CreateForm.this.validate ()) {
                        Name name = set (new Name ());
                        DomGlobal.alert (
                            Controls.safe (name.first, "")
                            + " " + Controls.safe (name.middle, "")
                            + " " + Controls.safe (name.last, ""));
                    }
                });
            }));
        });
        updateBtn.disable ();

        addListener (IModifiedListener.create (ctl -> {
            if (dirty ())
                updateBtn.enable ();
            else
                updateBtn.disable ();
        }));
    }
    
}
```

First we note the addition of the `IModifiedListener` at the end of the code block. This listens to changes in controls and when a change is detected it determines if any of the controls in the form are dirty and if so enables the *Create* button. Now when the *Create* button is clicked on we call the following:

```java
Name name = set (new Name ());
```

This creates a new instance of the `Name` class and passes it the control forms `set(...)` method. If we take a look at the class declaration we see that it extends:

```java
ControlForm<Void,Name>
```

where `Name` appears in the second parameter. This is the so-called *destination* type for the form and an instance of which is passed through to the `set(...)` method. It is also the type we expect when we declare setters for each of the cells:

```java
cell.set ((ctx,v,n) -> n.first = v);
```

This informs the form that when the `set(...)` method is called it should invoke all the declared setters passing through an `ISetterContext`, the control value and the object passed to `set(...)`. In the example above this assigns the value of the (*First name*) control to the `first` member of the `Name` instance.  Once set the button invocation then opens an alert dialog displaying the full name.

?> The `ISetterContext` provides a range of methods to access other controls, this means that one can access those controls to determine what should be set (in cases where there is some form of contingency to be accounted for or composition that is required).

We now look briefly at option (2). Here we drop the `cell.set ((ctx,v,n) -> n.first = v);` assignments and replace the button invocation with:

```java
// Create person form (second variant)
Name name = new Name ();
name.first = CreateFormVariant.this.value ("firstName");
name.middle = CreateFormVariant.this.value ("middleName");
name.last = CreateFormVariant.this.value ("lastName");
DomGlobal.alert (Controls.safe (name.first, "") + " " + Controls.safe (name.middle, "") + " " + Controls.safe (name.last, ""));
```

In this case we make use of the control forms `value(String)` method that returns the value of the control corresponding to the passed reference. As noted above this is not type safe but is convenient.

### Update form

**Refer to `UpdateForm`**

We build on the previous example and enable the form to update an existing name:

```java
// Update person form
public class UpdateForm extends ControlForm<Name,Name> {

    private Button updateBtn;
    
    public UpdateForm() {
        super (ControlFormCreator.create ().maxWidth (Length.px (600)));

        group (grp -> {
            grp.row (row -> {
                row.control ("firstName", "First name", Controls.text (cfg -> {
                    cfg.placeholder ("First name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                    cell.set ((ctx,v,n) -> n.first = v);
                    cell.get (n -> n.first);
                });
                row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                    cfg.placeholder ("Middle name(s)");
                    cfg.validator (
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1);
                    cell.set ((ctx,v,n) -> n.middle = v);
                    cell.get (n -> n.middle);
                });
                row.control ("lastName", "Last name", Controls.text (cfg -> {
                    cfg.placeholder ("Last name");
                    cfg.validator (
                        Validators.notEmpty ("Cannot be empty"),
                        Validators.length (0, 40, "Cannot be more that 40 character")
                    );
                }), cell -> {
                    cell.grow (1).required ();
                    cell.set ((ctx,v,n) -> n.last = v);
                    cell.get (n -> n.last);
                });
            });
        });
        bar (bar -> {
            bar.add (ButtonCreator.build (cfg -> {
                cfg.label ("load").style (Button.Config.Style.LINK);
                cfg.handler (() -> {
                    UpdateForm.this.reset ();
                    Name name = new Name ();
                    String[] randomName = Names.NAMES[Random.nextInt (99)].split (" ");
                    name.first = randomName[0];
                    name.last = randomName[1];
                    get (name);
                });
            }));
            bar.add (updateBtn = ButtonCreator.build (cfg -> {
                cfg.label ("Create");
                cfg.handler (() -> {
                    if (UpdateForm.this.validate ()) {
                        Name name = set (new Name ());
                        DomGlobal.alert (Controls.safe (name.first, "") + " " + Controls.safe (name.middle, "") + " " + Controls.safe (name.last, ""));
                    }
                });
            }));
        });
        updateBtn.disable ();

        addListener(IModifiedListener.create (ctl -> {
            if (dirty())
                updateBtn.enable ();
            else
                updateBtn.disable ();
        }));
    }
    
}
```

The first thing one should note is that we now introduce a parameterisation of `Name` in the first place of `ControlForm<Name,Name>`. This allows us to pass a name object to the controls `get(...)` method which is then propagated to each control via a declared *getter*:

```java
cell.get (n -> n.last);
```

The getter is responsible for extracting data from the passed object and returning the value that the control should take. In the example the form is populated by clicking on the *load* button. This extracts a random name (from `Names`) and uses the name to assign values to the first and last name components of a `Name` instance. This is then passed through to the form which populates the first and last name controls. If yoy make a change then the *Create* button enables which will display the changes when actioned.

?> You will note that the dialog that appears when you click on *Create* actually only displays the name fields you have actually changed. This is due to the default behaviour that the setters are only invoked for controls that are dirty meaning that new `Name` instance you passed only gets populated which changes. Often this is sufficent (where only changes are being applied) but not always. You can change this as a default by setting `setterApplyWhenNotDirty (true)` on the forms configuration (which is a global change) or on a per-cell basis with `cell.set (..., true);`. Try this as see what the effect is.

### Remote errors

**Refer to `CreateFormWithErrors`**

The final topic we cover is error handling when errors are returned remotely. Here we assume that every error messages returned contains at least the body of the message and some target to which it applies. These can be encapsulated in an instanceof `IErrorMessage` which can then be passed to the form. We modify our name form so that when we click on *Create* we mimic a failed action with error messages being returned:

```java
// Create person form with errors
bar.add (updateBtn = ButtonCreator.build (cfg -> {
    cfg.label ("Create");
    cfg.handler (() -> {
        if (CreateFormWithErrors.this.validate ()) {
            List<IErrorMessage> messages = new ArrayList<>();
            messages.add(IErrorMessage.create ("first_name", -1, "Some error occurred"));
            messages.add(IErrorMessage.create ("middle_name", -1, "Some error occurred"));
            messages.add(IErrorMessage.create ("system", -1, "A system error occurred"));
            CreateFormWithErrors.this.invalidate (messages);
        }
    });
}));
```

Note that we have left the form validation in place so when you test this out you need to at least provide some content for the first and last names (those being required) to allow the form validation to pass. In our mock up we generate three error messages two of which are targetted to specific content (the first and middle) names as one that is generic. Having created the errors we pass them through to the forms `invalidate(...)` method which is responsible for presentation.

When a form receives error messages this way it first tries to distribute the messages to each of the controls (by way of their `invalidate(...)` methods). A control may *accept* a message (or multiple messages) which transition the state to *invalid* and result in the firing if an invalidation event that carries the accepted messages (the form then displays these messages just as in the case of a form validation). These accepted messages are removed from the list of messages. Once all controls have been passed over any remaining messages are displayed in the main message body at the top of the form.

You should note that the targets are not named in a fashion that is reflected in our form to date (and this will be entirely typical) and we need to perform some mappings. This can be achieved by directing each control to *accept* error messages by specifying a path:

```java
cfg.acceptor ("first_name");
```

?>Try creating different error messages with different targets (paths). A control can be configured to accept more than one message target.

## Part D: Custom controls

**Refer to `RatingControl`**.

Here we are going to build a simple rating control that yields an integer based on a number selected from a range.

### Outlining the component

We begin by providing an outline control `RatingControl` that extends `Control` (being the common base class for controls):

```java
public static class RatingControl extends Control<Integer,RatingControl.Config> {

    public static class Config extends Control.Config<Integer,Config> {

        private int range = 1;

        public Config range(int range) {
            this.range = Math.max (1, range);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public RatingControl build(LayoutData... data) {
            return build (new RatingControl (this), data);
        }

    }
    
    public RatingControl(Config config) {
        super(config);
    }

    @Override
    protected Integer valueFromSource() {
        return null;
    }

    @Override
    protected void valueToSource(Integer value) {
        
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).text ("Control");
        }).build ();
    }
}
```

Since custom component (controls included) tend to be re-used we have adopted the more formal approach that uses a configuration class `Config`. We make the following observations:

1. The `Control` class is parameterised over two types, one be the value type and the other being the configuration class. Note the value type is `Integer`.
2. We have one configuration parameter and that is the range (the number of options to select from starting from 1).
3. We provide a placeholder `buildNode(Element)` method that will be expanded up next.
4. We override two mandatory methods `valueFromSource()` and `valueToSource(Integer)`. These are used during the lifecycle of the control to update the controls presentation to reflect any assigned value and to retrieve a value from the presentation when there is a user-initiated update to the UI.

This can be added to the constructor of `Lesson4d` as follows:

```java
add (new RatingControl.Config().range(5).build());
```

where we have used an arbitrary range of 5.

### Building out the presentation

We would like to render out a single box for each option from 1 to the specified range. If the control holds a value then the corresponding box is highlighted. Likewise, when the user clicks on a box then that becomes the selected value.

The following implements this behaviour in a manner that employs direct DOM manipulation (in particular the state of the value of the control presentation-side is encoded in the state of the DOM - namely the presence of the `.selected` CSS class):

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (root -> {
        root.style ("lesson4 rating");
        for (int i = 1; i <= config().range; i++) {
            Div.$ (root)
                // Store a reference to the value in the "item" attribute.
                .attr ("item", "" + i)
                // Display the value in the DIV.
                .text ("" + i)
                // Enable click handling for user interaction.
                .onclick((e,n) -> {
                    // Iterate over the siblings of the node and remove the selected
                    // class.
                    JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                    // Add the selected class to the now selected item.
                    JQuery.$ (n).addClass("selected");
                });
        }
    }).build ();
}
```

The CSS for this is contained in `lessons.css` and is included below for reference:

```css
.lessons .lesson4.rating {
    display: flex;
    gap: 0.25em;
}

.lessons .lesson4.rating > div {
    width: 2em;
    height: 2em;
    border: 1px solid #ccc;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-weight: 500;
}

.lessons .lesson4.rating > div:hover {
    background-color: #f1f1f1;
}

.lessons .lesson4.rating > div.selected {
    background-color: #ccc;
}
```

In operation we see that numbered boxes appear horizontally (initially with none selected) and that we can select (and change selection) any of the boxes. Selection is indicated by the `.selected` CSS class.

We now need to wire this into the value mechanism.

### Assigning a value to the control

We first consider the flow associated with value assignment. We can modify the inclusion code to assign an initial value:

```java
RatingControl ctl = add (new RatingControl.Config().range(5).build());
ctl.setValue (3);
```

And update `RatingControl` to implement the value-to-source flow:

```java
@Override
protected void valueToSource(Integer value) {
    // Clear any selected items.
    JQuery.$ (getRoot()).find ("div").removeClass ("selected");
    // Find the item we are interested in and select it.
    if (value != null)
        JQuery.$ (getRoot()).find("div[item=" + value + "]").addClass("selected");
}
```
Here we make use of the fact the the boxes are DIV's that reside directly under the controls root DIV, which is obtainable from a call to `getRoot()`. First we clear all selected items by removing any `.selected` CSS class then we pick up the box corresponding to our value (as encoded in the `item` attribute) and apply the `.selected` class to that.

In operation we see that option 3 is selected. Now we implement the user interaction flow.

### Responding to a user selection

To finish the control we need to update the controls value when the user selects an item. We modify the inclusion code to add a listener so we can observe the response:

```java
RatingControl ctl = add (new RatingControl.Config().range(5).build());
ctl.setValue(3);
ctl.handleControlModified (c -> Logger.info ("Value: " + c.value ()));
```

Now we turn to the `buildNode(...)` method. We already have a response when the user clicks, the response being to reconfigure the DOM. At this point we can notify the control that a modification has occurred by making a call to `modified()`:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$ (el).$ (root -> {
        root.style ("lesson4 rating");
        for (int i = 1; i <= config().range; i++) {
            Div.$ (root)
                .attr ("item", "" + i)
                .text ("" + i)
                .onclick((e,n) -> {
                    JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                    JQuery.$ (n).addClass("selected");
                    // After updating the DOM we notify the control that a change
                    // has occurred.
                    modified ();
                });
        }
    }).build ();
}
```

The control responded to `modified()` by initiating the value-retrieval-flow which involves a call to `valueFromSource()`, which is expected to return the newly selected value. Since we have already updated the DOM we can simply inspect it:

```java
@Override
protected Integer valueFromSource() {
    JQueryElement el = JQuery.$ (getRoot()).find ("div.selected");
    if (el.length() > 0) {
        String item = el.get(0).getAttribute("item");
        return Integer.parseInt (item);
    }
    return null;
}
```

Here we find the selected box and return the value present on its `item` attribute. In operation we see the effect of selecting item in the logging console.

We leave further modification to the exercises.

### More complicated controls

We won't explore these here but you are encouraged to read [Controls](ess_controls.md#custom-controls) for a more full exposition of creating custom controls, in particular value preparation and cloning (the latter being important when dealing with list-based values). You are also encourged to look at the various standard JUI controls for further guidance on control creation.

### Exercises

Making use of the `RatingControl` created in this part to apply the following modifications:

1. The control as given accesses the box DIVs directly off the root element. In a more complex arrangement the boxes may live under quite a different element. How would you accommodate this?
2. The control as given makes use of direct DOM manipulation, can you revise the control so that it makes use of component re-rendering?
3. Modify the control to allow for the de-selection of a selected item.

## Solutions to exercises

Here we present candidate solutions to the exercises presented in each of the parts. Note that these are (in general) only one of many possible solutions.

### Part A

Exercies are:

1. You have used the creator methods on `Controls` to create the various controls. How would you create a control without using `Controls`? 
2. Create a component that displays two text controls such that when you update one the other reflects the changes you make (here make sure you set values quietly, as in the last example of [Control values](#control-values)). Use the render in constructor approach (see [Lesson 1: Render in constructor](lessons_1.md#render-in-constructor)). Do you need to do the update quietly?
3. Modify exercise (2) so that you use a delayed modifier (similar to what was used in [Search and filtering](#search-and-filtering)). What do you notice about the effect of the delay?
4. The `TextControl` has the configuration option `expandOnFocus(Length)` that will expand the height of the control when it gains focus (to the specified length) and revert back to the default height when it looses focus. Assume this option was not available but you wanted the control to behave that way; how could you achieve that? (assume that setting the height of the control to `null` reverts to the default height).

#### Exercise 1

Consider a simple case:

```java
Controls.text (cfg -> {
    cfg.width (Length.px (150));
})
```

We can gain a hint by looking at the implementation of `Controls.text` which yields the following alternative:

```java
TextControlCreator.build (cfg -> {
    cfg.width (Length.px (150));
});
```

This is not much different but recall that the benefit of `Controls` is that all the control types are in one place. Now you could have gone directly to the components configuration:

```java
new TextControl.Config ()
    .width (Length.px (150))
    .build ();
```

This works equally as well and if you are creating your own components is likely the approach you would take for those.

#### Exercise 2

A very simple version follows. Note the use of `Value.of (val).quiet ()` which performs a quiet update.

```java
public class Exercise2 extends SimpleComponent {

    private TextControl field1Ctl;

    private TextControl field2Ctl;

    public Exercise2() {
        renderer (root -> {
            root.style ("lesson4", "panel");
            H3.$ (root).text ("Candidate solution for exercise 2");
            Div.$ (root).css ("display: flex; gap: 1em;").$ (
                field1Ctl = Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.modifiedHandler((c,val,prior) -> {
                        field2Ctl.setValue (Value.of (val).quiet());
                    });
                }),
                field2Ctl = Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.modifiedHandler((c,val,prior) -> {
                        field1Ctl.setValue (Value.of (val).quiet());
                    });
                })
            );
        });
    }
}
```

The second part of the exercise asks whether quiet is necessary? In terms of good practice and defensive programming, yes. Lets see what happens if you done quietly update. You enter a letter into the first control. This generates a modification event that is picked up and sets the value of the second control. If this is not quite then the second control generates a modifucation event and that then assigns a value back on the first control. In this case the value assigned is the same as the value already present and a change is *not* detected (and so no modification event is generated). However, this relies on this specific behaviour to prevent a runaway.

If you want to see the effect of a runaway, change `quiet` to `force` and look ni the console when you entery something into one of the controls.

#### Exercise 3

We can apply the delay to the first field:

```java
public class Exercise3 extends SimpleComponent {

    private TextControl field1Ctl;

    private TextControl field2Ctl;

    public Exercise3() {
        renderer (root -> {
            root.style ("lesson4", "panel");
            H3.$ (root).text ("Candidate solution for exercise 3");
            Div.$ (root).css ("display: flex; gap: 1em;").$ (
                field1Ctl = Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.modifiedHandler(DelayedModifiedHandler.create (300, (c,val,prior) -> {
                        field2Ctl.setValue (Value.of (val).quiet());
                    }));
                }),
                field2Ctl = Controls.text (cfg -> {
                    cfg.width (Length.px (150));
                    cfg.modifiedHandler((c,val,prior) -> {
                        field1Ctl.setValue (Value.of (val).quiet());
                    });
                })
            );
        });
    }
}
```

The effect is a clear and significant delay when you update the first field before the changes appear in the second (the reverse updates immediately).

#### Exercise 4

We can achieve this by adding a focus/blur listener:

```java
// Create a control with one row (the usual case when you employ an
// expansion like this, so it takes up minimal height in its blurred
// state).
TextAreaControl ctl = Controls.textarea(cfg -> {
    cfg.row(1);
});

// Add a listener (using the convenience `create` method in this case).
// When focus is gained then we set the control height to some value
// (5em here). When blurred we default the height (by settting to `null`).
ctl.addListener(IFocusBlurListener.create(focus -> {
    ctl.height(Length.em(5));
}, blr -> {
    ctl.height(null);
}));
```

### Part D

The exercises are:

1. The control as given accesses the box DIVs directly off the root element. In a more complex arrangement the boxes may live under quite a different element. How would you accommodate this?
2. The control as given makes use of direct DOM manipulation, can you revise the control so that it makes use of component re-rendering?
3. Modify the control to allow for the de-selection of a selected item.

#### Exercise 1

For this we extends `RatingControl` to `RatingControlExercise1` so that we can focus only on the relevant changes. Since we are using the configuration from `RatingControl` we need to modify how we instantiate the excercise class:

```java
RatingControlExercise1 ctl2 = add (new RatingControlExercise1 (new RatingControl.Config().range(5)));
ctl2.setValue(3);
ctl2.handleControlModified (c -> Logger.info ("Value: " + c.value ()));
```

The relevant changes are then:

```java
public static class RatingControlExercise1 extends RatingControl{

    public RatingControlExercise1(RatingControl.Config config) {
        super(config);
    }

    @Override
    protected Integer valueFromSource() {
        JQueryElement el = JQuery.$ (boxesEl).find ("div.selected");
        if (el.length() > 0) {
            String item = el.get(0).getAttribute("item");
            return Integer.parseInt (item);
        }
        return null;
    }

    @Override
    protected void valueToSource(Integer value) {
        JQuery.$ (boxesEl).find ("div").removeClass ("selected");
        if (value != null)
            JQuery.$ (boxesEl).find("div[item=" + value + "]").addClass("selected");
    }

    private Element boxesEl;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4 rating");
            root.by ("boxes");
            for (int i = 1; i <= config().range; i++) {
                Div.$ (root)
                    .attr ("item", "" + i)
                    .text ("" + i)
                    .onclick((e,n) -> {
                        JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                        JQuery.$ (n).addClass("selected");
                        modified ();
                    });
            }
        }).build (dom -> {
            boxesEl = dom.first ("boxes");
        });
    }
}
```

In effect we have just extracted the element that contains the boxes (still the root element in this example, but could be otherwise) into the `boxesEl` via a `by(...)` reference mechanism. We then use that element instead of `getRoot()`.

#### Exercise 2

Similarly to exercise 1 we extend `RatingControl`:

```java
public static class RatingControlExercise2 extends RatingControl {

    public RatingControlExercise2(RatingControl.Config config) {
        super(config);
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    private Integer value;

    @Override
    protected Integer valueFromSource() {
        // Just return the presetation-side value.
        return value;
    }

    @Override
    protected void valueToSource(Integer value) {
        // Don't re-render unless the value actually has changed.
        if (ComparisonSupport.equal(value, this.value))
            return;
        this.value = value;
        rerender();
    }

    /************************************************************************
     * Render and DOM.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4 rating");
            for (int i = 1; i <= config().range; i++) {
                // We need an effective 'final' value for i to be used in
                // the enclosed lambda-expression.
                int idx = i;
                Div.$ (root).$ (box -> {
                    box.text ("" + idx);
                    if ((value != null) && value.equals(idx))
                        box.style ("selected");
                    box.onclick((e,n) -> {
                        // Update the presentation-side value signal a
                        // modification then re-render.
                        value = idx;
                        modified ();
                        rerender ();
                    });
                });
            }
        }).build ();
    }
}
```

In this case we need to maintain a version of the value that is used presentation-side. It is this value that is used to mark the selected item on re-render. We need to force a re-render when we push value-to-source. When the user clicks on a box we update the presentation-side value, signal a modification (so `valueFromSource()` only needs to return the presentation-side value) then re-render.

#### Exercise 3

Similarly to exercise 1 we extend `RatingControl`:

```java
public static class RatingControlExercise3 extends RatingControl {

    public RatingControlExercise3(RatingControl.Config config) {
        super(config);
    }

    /************************************************************************
     * Render and DOM.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            root.style ("lesson4 rating");
            for (int i = 1; i <= config().range; i++) {
                Div.$ (root)
                    .attr ("item", "" + i)
                    .text ("" + i)
                    .onclick((e,n) -> {
                        if (JQuery.$ (n).hasClass("selected")) {
                            JQuery.$ (n).removeClass("selected");
                        } else {
                            JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                            JQuery.$ (n).addClass("selected");
                        }
                        modified ();
                    });
            }
        }).build ();
    }
}
```

We have simply modified the click handler to check if the item was already selected and if so remove the `.selected` class (otherwise proceed as normal).