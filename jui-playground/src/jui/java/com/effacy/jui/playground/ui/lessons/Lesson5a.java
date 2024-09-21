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
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.platform.util.client.Carrier;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.modal.ModalDialog;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.validation.model.validator.Validators;

public class Lesson5a extends Panel implements INavigationAware {

    private Button btn1;

    private Button btn2;

    protected Lesson5a() {
        super (PanelCreator.config ().scrollable ().padding(Insets.em(2)).layout (VertLayout.$ ().spacing (Length.em (1)).build ()));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part A: Inline modals", header -> {
                header.subtitle ("This lession describes how to make use of inline modals (which essentially take a component and wrap the component in a modal dialog).");
            });
        })).update (null);

        // Variant 1: Open a dialog with confirmation button to close.
        add (ButtonCreator.build (cfg -> {
            cfg.label ("Open modal");
            cfg.handler (() -> {
                ModalDialogCreator.build (ComponentCreator.build (p -> {
                    p.css (CSS.MARGIN, Insets.em (2)).text ("Hello!");
                }), mcfg -> {
                    mcfg.removeOnClose ();
                    mcfg.title ("Inline dialog");
                    mcfg.width (Length.px (300));
                    mcfg.action (action -> {
                        action.label ("Confirm");
                    });
                }).open ();
            });
        }));

        // Variant 2: Retaining a dialog for repeated use.
        add (ButtonCreator.build (cfg -> {
            final ModalDialog<?> dialog = ModalDialogCreator.build (ComponentCreator.build (p -> {
                p.css (CSS.MARGIN, Insets.em (2)).text ("Hello!");
            }), mcfg -> {
                mcfg.title ("Inline dialog");
                mcfg.width (Length.px (300));
                mcfg.action (action -> {
                    action.label ("Confirm");
                });
            });
            cfg.label ("Open modal");
            cfg.handler (() -> dialog.open ());
        }));

        // Variant 3: Updates label using a counter.
        add (btn1 = ButtonCreator.build (cfg -> {
            Carrier<Integer> count = Carrier.of (1);
            cfg.label ("Open modal");
            cfg.handler (() -> {
                // Display a dialog the renders a custom component.
                ModalDialogCreator.build (ComponentCreator.build (root -> {
                    root.p ().css (CSS.MARGIN, Insets.em (2)).text ("Click the button to update the label");
                }), mcfg -> {
                    mcfg.removeOnClose ();
                    mcfg.title ("Change button label");
                    mcfg.width (Length.px (300));
                    mcfg.action (action -> {
                        action.label ("close");
                        action.link ();
                    });
                    mcfg.action (action -> {
                        action.label ("Change label");
                        action.handler (hcb -> {
                            btn1.updateLabel ("Label changed " + count.get (v -> v + 1));
                            hcb.success ();
                        });
                    });
                }).open ();
            });
        }));

        // Variant 4: Allows use to enter a new label.
        add (btn2 = ButtonCreator.build (cfg -> {
            cfg.label ("Open modal");
            cfg.handler (() -> {
                // Display a dialog the renders a control form.
                ModalDialogCreator.build (ControlFormCreator.build (ccfg -> {
                    ControlFormCreator.configureForDialog (ccfg);
                }, panel -> {
                    panel.group (sec -> {
                        sec.group (gcfg -> {
                            gcfg.control ("label", "New button label", Controls.text (tcfg -> {
                                tcfg.validator (
                                    Validators.notEmpty ("Please provide a new label"),
                                    Validators.length (0, 40, "Cannot be more that 40 characters")
                                );
                            }), cell -> {
                                cell.grow (1)
                                    .required ()
                                    .guidance ("The label of the button that opened this dialog will be updated with this text."); 
                            });
                        });
                    });
                }), mcfg -> {
                    // Configure the dialog.
                    mcfg.removeOnClose ();
                    mcfg.title ("Update button label");
                    mcfg.width (Length.px (400));
                    mcfg.action (action -> {
                        // The close action.
                        action.label ("close");
                        action.link ();
                    });
                    mcfg.action (action -> {
                        // The change label action.
                        action.label ("Change label");
                        action.handler (hcb -> {
                            if (!hcb.contents ().validate ()) {
                                hcb.fail ();
                            } else {
                                String newLabel = (String) hcb.contents().value ("label");
                                btn2.updateLabel (newLabel);
                                hcb.success ();
                            }
                        });
                    });
                }).open ();
            });
        }));
    }

}
