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

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.validation.model.validator.Validators;

import elemental2.dom.DomGlobal;

public class Lesson4b extends Panel implements INavigationAware {

    public Lesson4b() {
        super (new Panel.Config ().scrollable ().layout (VertLayout.$ ().separator(Separator.LINE).spacing (Length.em (1)).build ()).padding (Insets.em (2)));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (0, 1.75, 0, 0.75)), builder -> {
            builder.header ("Part B: Control forms", header -> {
                header.subtitle ("This lesson explores controls in the context of forms.");
            });
        })).update (null);

        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Form A");
            root.insert (new FormA ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Form B");
            root.insert (new FormB ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Form C");
            root.insert (new FormC ());
        }));
        add (ComponentCreator.build (root -> {
            H3.$ (root).text ("Form D");
            root.insert (new FormD ());
        }));
        
    }

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

    public class FormB extends ControlForm<Void,Void> {

        public FormB() {
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
            });
            bar (bar -> {
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("clear").style (Button.Config.Style.LINK);
                    cfg.handler (() -> {
                        FormB.this.reset ();
                    });
                }));
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("Create");
                    cfg.handler (() -> {
                        if (FormB.this.validate())
                            DomGlobal.alert ("Success!");
                    });
                }));
            });
        }
    }

    public class FormC extends ControlForm<Void,Void> {

        public FormC() {
            super (ControlFormCreator.create ().maxWidth (Length.px (600)));

            group (grp -> {
                grp.header (header -> {
                    header.title ("Name and salutation");
                    header.instruction ("Provide the persons name and their salutation.");
                });
                grp.footer(footer -> {
                    footer.guidance ("Note that the title will be used in all communications with the person.");
                });
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
                        cell.guidance ("Try to keep this as brief as possible.");
                    });
                });
            });
            bar (bar -> {
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("clear").style (Button.Config.Style.LINK);
                    cfg.handler (() -> {
                        FormC.this.reset ();
                    });
                }));
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("Create");
                    cfg.handler (() -> {
                        if (FormC.this.validate())
                            DomGlobal.alert ("Success!");
                    });
                }));
            });
        }
    }

    public class FormD extends ControlForm<Void,Void> {

        public FormD() {
            super (ControlFormCreator.create ().maxWidth (Length.px (600)));

            group (grp -> {
                grp.header (header -> {
                    header.title ("Name and salutation");
                    header.instruction ("Provide the persons name and their salutation.");
                });
                grp.footer(footer -> {
                    footer.guidance ("Note that the title will be used in all communications with the person.");
                });
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
                        cell.guidance ("Try to keep this as brief as possible.");
                    });
                });
            });
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
            bar (bar -> {
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("clear").style (Button.Config.Style.LINK);
                    cfg.handler (() -> {
                        FormD.this.reset ();
                    });
                }));
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("Create");
                    cfg.handler (() -> {
                        if (FormD.this.validate())
                            DomGlobal.alert ("Success!");
                    });
                }));
            });
        }
    }
    
}
