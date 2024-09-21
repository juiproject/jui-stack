/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.playground.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.control.IInvalidListener;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.validator.LengthValidator;
import com.effacy.jui.validation.model.validator.NotEmptyValidator;
import com.effacy.jui.validation.model.validator.Validators;

import elemental2.dom.Element;

public class DynamicControl extends Panel {

    private TextControl otherCtl;

    public DynamicControl() {
        super (new Panel.Config ()
            .scrollable ()
            .layout (VertLayoutCreator.create (Length.em(2)))
            .padding (Insets.em (2))
            .width (Length.px (580)));

        ControlForm<Void,Void> form = add (ControlFormCreator.<Void,Void> build (cfg ->  {

        }, builder -> {
            builder.group (section -> {
                section.header (adorn -> {
                    adorn.title ("Conact details");
                });
                section.row (row -> {
                    row.control ("Title", Controls.<String> selector (cfg -> {
                        cfg.allowSearch (false)
                            .selectorHeight (Length.em (13))
                            .modifiedHandler( (ctl, val, prior) -> {
                            if ("Other".equals (val)) {
                                otherCtl.enable ();
                            } else {
                                otherCtl.setValue ("");
                                otherCtl.disable ();
                            }
                        })
                        .validator (NotEmptyValidator.validator ("Please select a title"))
                        .acceptor ("title")
                        .width (Length.em (10));
                    }, "Mr", "Mrs", "Ms", "Miss", "Mx", "Master", "Other"), handler -> {
                        handler.required ();
                    });
                    row.control ("Other title (or salutation)", otherCtl = Controls.text (cfg -> {
                        cfg.placeholder ("Other title")
                            .validator (NotEmptyValidator.validator ("Please provider an alternative title to use"))
                            .acceptor ("titleOther");
                    }), handler -> handler.grow (1));
                    otherCtl.disable ();
                });
                section.row (row -> {
                    row.control ("First name", Controls.text (cfg -> {
                        cfg.placeholder ("First name")
                            .acceptor ("firstName")
                            .validator (Validators.notEmpty ("Cannot be empty"));
                    }), handler -> {
                        handler.required ()
                            .help ("The first name of the person");
                    });
                    row.control ("Middle name(s)", Controls.text (cfg -> {
                        cfg.placeholder ("Middle name(s)")
                            .acceptor ("middleName");
                    }));
                    row.control ("Last name", Controls.text (cfg -> {
                        cfg.placeholder ("Last name")
                            .acceptor ("lastName")
                            .validator (Validators.notEmpty ("Cannot be empty"));
                    }), handler -> {
                        handler.required();
                    });
                });
            });
        }));

        add (PanelCreator.buttonBar (cfg -> cfg.width (Length.em (40)), bar -> {
            bar.add (ButtonCreator.build (btn -> {
                btn.label ("Save");
                btn.handler (() -> {
                    if (form.validate (true)) {
                        // Fake an error message.
                        List<IErrorMessage> errors = new ArrayList<> ();
                        errors.add (new ErrorMessage ("firstName", "Not a valid first name"));
                        errors.add (new ErrorMessage ("middleName", "Not a valid middle name"));
                        errors.add (new ErrorMessage ("lastName", "Not a valid last name"));
                        errors.add (new ErrorMessage (null, "An unknown systen error occurred"));
                        form.invalidate (errors);
                    }
                });
            }), ActionBarLayout.data (1));
        }));

        add (ComponentCreator.build (root -> {
            TextControl ctl;
            Cpt.$ (root, ctl = new TextControl.Config ()
                .modifiedHandler ((c, val, prior) -> {
                    Logger.info ("Value set: " + val);
                }).build ()
            );
            Button.$ (root).text ("Reset").onclick (e -> {
                ctl.reset ();
            });
            ctl.setValue (Value.of ("Initial value").force ());
        }));

        add (ComponentCreator.build (root -> {
            TextControl ctl;
            Cpt.$ (root, ctl = new TextControl.Config ()
                .validator (
                    NotEmptyValidator.validator ("Cannot be empty"),
                    LengthValidator.validator (0, 10, "Cannot be more that {max} characters")
                )
                .build ()
            );
            Button.$ (root).text ("Validate").onclick (e -> {
                if (!ctl.validate ())
                    Logger.error ("INVALID! " + ctl.invalidator ().messages ().get (0));
            });
        }));

        add (ComponentCreator.build (root -> {
            TextControl ctl1;
            TextControl ctl2;
            Div.$ (root).css ("display", "flex").css ("gap", "1em").$ (
                Cpt.$ (ctl1 = new TextControl.Config ()
                    .placeholder ("First name")
                    .acceptor ("firstName")
                    .build ()),
                Cpt.$ (ctl2 = new TextControl.Config ()
                    .placeholder ("Last name")
                    .acceptor ("lastName")
                    .build ()),
                Button.$ ().text ("Validate").onclick (e -> {
                    List<IErrorMessage> errors = new ArrayList<> ();
                    errors.add (new ErrorMessage ("firstName", "Not a valid first name"));
                    errors.add (new ErrorMessage ("lastName", "Not a valid last name"));
                    ctl1.invalidator ().accept (errors);
                    ctl2.invalidator ().accept (errors);
                })
            );
            ctl1.addListener (IInvalidListener.create ((ctl,errors) -> {
                Logger.error ("First name: " + errors.get(0));
            }, ctl -> {}));
            ctl2.addListener (IInvalidListener.create ((ctl,errors) -> {
                Logger.error ("Last name: " + errors.get(0));
            }, ctl -> {}));
        }));

        add (ComponentCreator.build (root -> {
            TextControl ctl;
            Cpt.$ (root, ctl = new TextControl.Config()
                .build ());
            ctl.waiting (true);
            Button.$ (root).text ("Load").onclick (e -> {
                ctl.setValue ("Loaded value");
                ctl.waiting (false);
            });
        }));

        add (new SimpleForm ());

        add (new LoadForm ());

        //add (new ExternalValidation());
    }

    public static class SimpleForm extends SimpleComponent {

        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                Div.$ (root).css ("display", "flex").css ("gap", "1em").$ (
                    Cpt.$ (new TextControl.Config ()
                        .placeholder ("First name")
                        .acceptor ("firstName")
                        .invalidationHandler((invalid,messages) -> {
                            if (invalid)
                                Logger.error ("First name: " + messages.get(0));
                        })
                        .build ()),
                    Cpt.$ (new TextControl.Config ()
                        .placeholder ("Last name")
                        .acceptor ("lastName")
                        .invalidationHandler((invalid,messages) -> {
                            if (invalid)
                                Logger.error ("Last name: " + messages.get(0));
                        })
                        .build ()),
                    Button.$ ().text ("Validate").onclick (e -> {
                        List<IErrorMessage> errors = new ArrayList<> ();
                        errors.add (new ErrorMessage ("firstName", "Not a valid first name"));
                        errors.add (new ErrorMessage ("lastName", "Not a valid last name"));
                        SimpleForm.this.controls ().accept (errors);
                    })
                );
            }).build ();
        }
    }
    
    public static class LoadForm extends SimpleComponent {

        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                Div.$ (root).css ("display", "flex").css ("gap", "1em").$ (
                    Cpt.$ (new TextControl.Config ()
                        .placeholder ("First name")
                        .name ("firstName")
                        .build ()),
                    Cpt.$ (new TextControl.Config ()
                        .placeholder ("Last name")
                        .name ("lastName")
                        .build ()),
                    Button.$ ().text ("Load").onclick (e -> {
                        LoadForm.this.controls ().waiting (false);
                        LoadForm.this.controls ().get ("firstName").setValue ("Jill");
                        LoadForm.this.controls ().get ("lastName").setValue ("Jones");
                    }),
                    Button.$ ().text ("Retrieve").onclick (e -> {
                        Logger.info ("First name: " + LoadForm.this.controls ().get ("firstName").value ());
                        Logger.info ("Second name: " + LoadForm.this.controls ().get ("lastName").value ());
                    })
                );
            }).build ();
        }

        @Override
        protected void onAfterRender() {
            controls().waiting (true);
        }
    }


    public static class ExternalValidation extends Panel {

        public ExternalValidation() {
            super (new Panel.Config ()
                .layout (VertLayoutCreator.create (Length.em (1))));

            ControlForm<Void,Void> form = add (ControlFormCreator.<Void,Void> build (builder -> {
                builder.group (section -> {
                    section.row (row -> {
                        row.control ("First name", Controls.text (cfg -> {
                            cfg.placeholder ("First name")
                                .acceptor ("firstName")
                                .validator (Validators.notEmpty ("Cannot be empty"));
                        }), handler -> {
                            handler.required ().grow (1);
                        });
                        row.control ("Last name", Controls.text (cfg -> {
                            cfg.placeholder ("Last name")
                                .acceptor ("lastName")
                                .validator (Validators.notEmpty ("Cannot be empty"));
                        }), handler -> {
                            handler.grow (1);
                            handler.required();
                        });
                    });
                });
            }));

            add (PanelCreator.buttonBar (cfg -> cfg.width (Length.em (40)), bar -> {
                bar.add (ButtonCreator.build (btn -> {
                    btn.label ("Save");
                    btn.handler (() -> {
                        if (form.validate (true)) {
                            // Fake an error message.
                            List<IErrorMessage> errors = new ArrayList<> ();
                            errors.add (new ErrorMessage ("firstName", "Not a valid first name"));
                            errors.add (new ErrorMessage ("lastName", "Not a valid last name"));
                            errors.add (new ErrorMessage (null, "An unknown systen error occurred"));
                            form.invalidate (errors);
                        }
                    });
                }), ActionBarLayout.data (1));
            }));
        }
    }

}

