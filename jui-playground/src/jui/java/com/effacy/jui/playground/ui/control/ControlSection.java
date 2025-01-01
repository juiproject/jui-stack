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

import java.util.Map;

import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.validation.model.validator.Validators;

public class ControlSection extends TabNavigator {

    public ControlSection() {
        super (TabNavigatorCreator.config ().style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE).padding (Insets.em (0)));

        tab ("simple", "Simple form", new ControlExamples ());
        tab ("dynamic", "Dynamic form", new DynamicControl ());
        tab ("other", "Other", PanelCreator.build (cfg-> cfg.scrollable (), panel -> {
            panel.add (PanelCreator.build (cfg -> {
                cfg.layout (VertLayoutCreator.create (Length.em(2)));
                cfg.padding (Insets.em (2));
                cfg.width (Length.px (580));
            }, subpanel -> {
                ControlForm<Map<String,Object>,Object> form = form();
                subpanel.add (form);
                subpanel.add(PanelCreator.buttonBarRightAligned (barcfg-> {
                }, bar-> {
                    bar.add(ButtonCreator.build(btncfg -> {
                        btncfg.style(Button.Config.Style.LINK);
                        btncfg.label("clear");
                        btncfg.handler(() -> {
                            form.reset();
                        });
                    }));
                    bar.add(ButtonCreator.build(btncfg -> {
                        btncfg.label("Update");
                        btncfg.handler(() -> {
                            form.validate();
                        });
                    }));
                }));
            }));
            
        }));
    }

    ControlForm<Map<String,Object>,Object> form() {
        return ControlFormCreator.<Map<String,Object>,Object> build (null, builder -> {
            builder.group (section -> {
                section.header (adorn -> {
                    adorn.title ("Persons name");
                    adorn.icon (FontAwesome.hatCowboy());
                    adorn.instruction ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.");
                });
                section.footer (adorn -> {
                    adorn.guidance ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt.");
                });
                section.row (row -> {
                    row.control ("First name", Controls.text (cfg -> {
                        cfg.placeholder ("First name")
                            .validator (Validators.notEmpty ("Cannot be empty"));
                    }), handler -> {
                        handler.required ()
                            .help ("The first name of the person")
                            .edit (src -> (String) src.get ("nameFirst"))
                            .modify((ctx, ctl) -> Logger.log ("CTL: "+ ctl.value()));
                    });
                    row.control ("Middle name(s)", Controls.text (cfg -> cfg.placeholder ("Middle name(s)")), handler -> {
                        handler
                            .edit (src -> (String) src.get("nameMiddle"));
                    });
                    row.control ("Last name", Controls.text (cfg -> {
                        cfg.placeholder ("Last name")
                            .validator (Validators.notEmpty ("Cannot be empty"));
                    }), handler -> {
                        handler.required()
                            .edit (src -> (String) src.get("nameLast"))
                            .retrieve ((ctx,val,dst) -> {});
                    });
                });
                section.row (row -> {
                    row.control ("Password", Controls.text (cfg -> {
                        cfg.placeholder ("Password")
                            .password ()
                            .validator (Validators.notEmpty ("Cannot be empty"));
                    }), handler -> {
                        handler.grow (1).required ()
                            .edit (map -> (String) map.get("nameLast"));
                    });
                    row.component (ButtonCreator.build (cfg -> cfg.style (Button.Config.Style.NORMAL).label ("Add").icon (FontAwesome.plus (FontAwesome.Option.BOLD)).handler (cb -> {
                        ControlDialog.open (outcome -> {
                            cb.complete ();
                        });
                    })), handler -> handler.offset (0, 4).grow (1));
                });
                section.row (row -> {
                    row.control("Title", Controls.selector (cfg -> {
                        cfg.allowSearch (false)
                            .selectorHeight (Length.em (13))
                            .validateOnModified ()
                            .validator (Validators.notEmpty ("Please select a title"))
                            .acceptor ("title")
                            .width (Length.em (10));
                    },
                        "Mr", "Mrs", "Ms", "Miss", "Mx", "Master", "Other"
                    ), handler -> {
                        handler.required();
                        handler.modify ((ctx,ctl) -> {
                            if ("Other".equals (ctl.value())) {
                                ctx.enable ("title_other");
                            } else {
                                ctx.set ("title_other", "");
                                ctx.disable ("title_other");
                            }
                        });
                    });
                    row.control("Other title (or salutation)", Controls.text(cfg -> {
                        cfg.placeholder ("First name");
                        cfg.validateOnModified ().validator (Validators.notEmpty ("Please provide a first name")).acceptor ("firstName");
                    }), handler -> {
                        handler.required ();
                        handler.by ("title_other").grow (1).disable ();
                    });
                });
            });

            builder.group (section -> {
                section.header(header -> {
                    header.title("Person stuff");
                    header.icon(FontAwesome.airFreshener());
                    header.instruction("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
                });
                section.group(collective -> {
                    collective.header(header -> header.title ("How to handle people"));
                    collective.group (group -> {
                        group.header (header -> {
                            header.title ("You you want to name this thing?");
                            header.instruction("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
                        });
                        group.conditional (cfg -> cfg.active());
                        group.row (row -> {
                            row.control ("First name", Controls.text (cfg -> {
                                cfg.placeholder ("First name")
                                    .validator (Validators.notEmpty ("Cannot be empty"));
                            }), handler -> {
                                handler.required ()
                                    .help ("The first name of the person")
                                    .edit (src -> (String) src.get ("nameFirst"))
                                    .modify((ctx, ctl) -> Logger.log ("CTL: "+ ctl.value()));
                            });
                            row.control ("Middle name(s)", Controls.text (cfg -> cfg.placeholder ("Middle name(s)")), handler -> {
                                handler
                                    .edit (src -> (String) src.get("nameMiddle"));
                            });
                            row.control ("Last name", Controls.text (cfg -> {
                                cfg.placeholder ("Last name")
                                    .validator (Validators.notEmpty ("Cannot be empty"));
                            }), handler -> {
                                handler.required()
                                    .edit (src -> (String) src.get("nameLast"))
                                    .retrieve ((ctx,val,dst) -> {});
                            });
                        });
                    });
                    collective.group (group -> {
                        group.header (header -> {
                            header.title ("You you want to name this other thing?");
                            header.instruction ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
                        });
                        group.conditional (cfg -> cfg.active (false).handler((ctx,open) -> {
                            ctx.set ("grp1.lastname", "Jones");
                        }));
                        group.row (row -> {
                            row.control ("First name", Controls.text (cfg -> {
                                cfg.placeholder ("First name")
                                    .validator (Validators.notEmpty ("Cannot be empty"));
                            }), handler -> {
                                handler.required ()
                                    .by ("grp1.firstname")
                                    .help ("The first name of the person")
                                    .edit (src -> (String) src.get ("nameFirst"))
                                    .modify((ctx, ctl) -> Logger.log ("CTL: "+ ctl.value()));
                            });
                            row.control ("Middle name(s)", Controls.text (cfg -> cfg.placeholder ("Middle name(s)")), handler -> {
                                handler
                                    .by ("grp1.middlename")
                                    .edit (src -> (String) src.get("nameMiddle"));
                            });
                            row.control ("Last name", Controls.text (cfg -> {
                                cfg.placeholder ("Last name")
                                    .validator (Validators.notEmpty ("Cannot be empty"));
                            }), handler -> {
                                handler.required()
                                    .by ("grp1.lastname")
                                    .edit (src -> (String) src.get("nameLast"))
                                    .retrieve ((ctx,val,dst) -> {});
                            });
                        });
                    });
                });
            });

        });
    }
}
