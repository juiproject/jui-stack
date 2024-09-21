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
package com.effacy.jui.playground.ui.lessons;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.control.IModifiedListener;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.util.Random;
import com.effacy.jui.playground.ui.tutorial.Names;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.validator.Validators;

import elemental2.dom.DomGlobal;

public class Lesson4c extends Panel {
    
    public Lesson4c() {
        super (new Panel.Config ().scrollable ().layout (VertLayout.$ ().spacing (Length.em (1)).build ()).padding (Insets.em (2)));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (0, 1.75, 0, 0.75)), builder -> {
            builder.header ("Part C: Data handling", header -> {
                header.subtitle ("This lesson explores how forms can handle data.");
            });
        })).update (null);

        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Create person form");
            root.insert (new CreateForm ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Create person form (second variant)");
            root.insert (new CreateFormVariant ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Update person form");
            root.insert (new UpdateForm ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Create person form with errors");
            root.insert (new CreateFormWithErrors ());
        }));
    }

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
                        cell.retrieve ((ctx,v,n) -> n.first = v);
                    });
                    row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                        cfg.placeholder ("Middle name(s)");
                        cfg.validator (
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                    }), cell -> {
                        cell.grow (1);
                        cell.retrieve ((ctx,v,n) -> n.middle = v);
                    });
                    row.control ("lastName", "Last name", Controls.text (cfg -> {
                        cfg.placeholder ("Last name");
                        cfg.validator (
                            Validators.notEmpty ("Cannot be empty"),
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                    }), cell -> {
                        cell.grow (1).required ();
                        cell.retrieve ((ctx,v,n) -> n.last = v);
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
                            Name name = retrieve (new Name ());
                            DomGlobal.alert (
                                Controls.safe (name.first, "")
                                + " " + Controls.safe (name.middle, "")
                                + " " + Controls.safe (name.last, ""));
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

    public class CreateFormVariant extends ControlForm<Void,Name> {

        private Button updateBtn;
        
        public CreateFormVariant() {
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
                        CreateFormVariant.this.reset ();
                    });
                }));
                bar.add (updateBtn = ButtonCreator.build (cfg -> {
                    cfg.label ("Create");
                    cfg.handler (() -> {
                        if (CreateFormVariant.this.validate ()) {
                            Name name = new Name ();
                            name.first = CreateFormVariant.this.value ("firstName");
                            name.middle = CreateFormVariant.this.value ("middleName");
                            name.last = CreateFormVariant.this.value ("lastName");
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
                        cell.retrieve ((ctx,v,n) -> n.first = v);
                        cell.edit (n -> n.first);
                    });
                    row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                        cfg.placeholder ("Middle name(s)");
                        cfg.validator (
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                    }), cell -> {
                        cell.grow (1);
                        cell.retrieve ((ctx,v,n) -> n.middle = v);
                        cell.edit (n -> n.middle);
                    });
                    row.control ("lastName", "Last name", Controls.text (cfg -> {
                        cfg.placeholder ("Last name");
                        cfg.validator (
                            Validators.notEmpty ("Cannot be empty"),
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                    }), cell -> {
                        cell.grow (1).required ();
                        cell.retrieve ((ctx,v,n) -> n.last = v);
                        cell.edit (n -> n.last);
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
                        edit (name);
                    });
                }));
                bar.add (updateBtn = ButtonCreator.build (cfg -> {
                    cfg.label ("Create");
                    cfg.handler (() -> {
                        if (UpdateForm.this.validate ()) {
                            Name name = retrieve (new Name ());
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

    public class CreateFormWithErrors extends ControlForm<Void,Name> {

        private Button updateBtn;
        
        public CreateFormWithErrors() {
            super (ControlFormCreator.create ().maxWidth (Length.px (600)));

            group (grp -> {
                grp.row (row -> {
                    row.control ("firstName", "First name", Controls.text (cfg -> {
                        cfg.placeholder ("First name");
                        cfg.validator (
                            Validators.notEmpty ("Cannot be empty"),
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                        cfg.acceptor ("first_name");
                    }), cell -> {
                        cell.grow (1).required ();
                        cell.retrieve ((ctx,v,n) -> n.first = v);
                    });
                    row.control ("middleName", "Middle name(s)", Controls.text (cfg -> {
                        cfg.placeholder ("Middle name(s)");
                        cfg.validator (
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                        cfg.acceptor ("middle_name");
                    }), cell -> {
                        cell.grow (1);
                        cell.retrieve ((ctx,v,n) -> n.middle = v);
                    });
                    row.control ("lastName", "Last name", Controls.text (cfg -> {
                        cfg.placeholder ("Last name");
                        cfg.validator (
                            Validators.notEmpty ("Cannot be empty"),
                            Validators.length (0, 40, "Cannot be more that 40 character")
                        );
                    }), cell -> {
                        cell.grow (1).required ();
                        cell.retrieve ((ctx,v,n) -> n.last = v);
                    });
                });
            });
            bar (bar -> {
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("clear").style (Button.Config.Style.LINK);
                    cfg.handler (() -> {
                        CreateFormWithErrors.this.reset ();
                    });
                }));
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

    public class Name {

        protected String first;

        protected String middle;

        protected String last;
        
    }
}
