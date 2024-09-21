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

import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IProcessable;
import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.SelectionGroupControl;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.Modal.Type;
import com.effacy.jui.ui.client.modal.ModalDialog.Config.ModalStyle;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.modal.ModalDialogCreator.IDialogOpener;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.validation.model.validator.Validators;

import elemental2.dom.DomGlobal;

public class Lesson5b extends Panel implements INavigationAware, IResetable {

    protected Lesson5b() {
        super (PanelCreator.config ().scrollable ().padding( Insets.em(2)).layout (VertLayout.$ ().separator (Separator.LINE).spacing (Length.em (1)).build ()));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part B: Modalising components", header -> {
                header.subtitle ("Here we describe how to take a component (i.e. a panel) and turn it into a modal.");
            });
        })).update (null);

        add (new SimpleFormPanel1 ());
        add (new SimpleFormPanel2 ());
        add (PanelCreator.buttonBar (null, bar -> {
            bar.add (ButtonCreator.config ().label ("Add project").handler(() -> {
                SimpleFormPanel3.open (outcome -> {
                    if (outcome.isPresent ())
                        DomGlobal.window.alert ("Project created!");
                });
            }).build ());
        }));
    }

    @Override
    public void onNavigateTo(NavigationContext context) {
        reset ();
    }


    public static class SimpleFormPanel1 extends Panel {

        private SelectionGroupControl<String> privacyCtl;

        private ControlForm<Void,Void> form;

        public SimpleFormPanel1() {
            super (new Panel.Config ().widthMax (Length.px (600)).layout (VertLayoutCreator.create (Length.em (0.75))).padding (Insets.em (2.5, 2)));
    
            add (form = ControlFormCreator.build (cfg -> cfg.startingDepth (1), builder -> {
                builder.group (grp -> {
                    grp.control ("name", "Name of project", Controls.text (cfg -> {
                        cfg.validator (Validators.notEmpty ("please enter the name of the project"));
                    }), cell -> cell.grow (1).required ());

                });
                builder.group (grp -> {
                    grp.control ("privacy", "Privacy", privacyCtl = Controls.radioGroup(cfg -> {
                        cfg.option ("option1", FontAwesome.users (), "Public to organisation", null)
                            .option ("option2", FontAwesome.lock (), "Private to me", null)
                            .validator (Validators.notEmpty ("please select an option"));
                    }), cell -> cell.required ());
                });
                privacyCtl.setValue (ListSupport.list ("option1"));
            }));
            
            add (PanelCreator.buttonBarRightAligned (null, bar -> {
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.style (Button.Config.Style.LINK);
                    cfg.label ("clear");
                    cfg.handler (() -> form.reset ());
                }));
                bar.add (ButtonCreator.build (cfg -> {
                    cfg.label ("Update");
                    cfg.handler (() -> {
                        if (form.validate ()) {
                            // Perform the save action here.
                            NotificationDialog.alert ("Saving information", "The information has been saved", t -> {
                                form.reset ();
                            });
                        }
                    }); 
                }));
            }));
        }
    }

    public static class SimpleFormPanel2 extends ControlForm<Void,Void> implements IProcessable<Object> {

        private SelectionGroupControl<String> privacyCtl;

        public SimpleFormPanel2() {
            super (ControlFormCreator.create ().maxWidth(Length.px (600)));
    
            group (grp -> {
                grp.control ("name", "Name of project", Controls.text (cfg -> {
                    cfg.validator (Validators.notEmpty ("please enter the name of the project"));
                }), cell -> cell.grow (1).required ());

            });
            group (grp -> {
                grp.control ("privacy", "Privacy", privacyCtl = Controls.radioGroup(cfg -> {
                    cfg.option ("option1", FontAwesome.users (), "Public to organisation", null)
                        .option ("option2", FontAwesome.lock (), "Private to me", null)
                        .validator (Validators.notEmpty ("please select an option"));
                }), cell -> cell.required ());
            });
            privacyCtl.setValue (ListSupport.list ("option1"));

            bar (bar -> {
                bar.add (ButtonCreator.config ().label ("Update").handler(() -> {
                    process(outcome -> {
                        if (!outcome.isEmpty ())
                            SimpleFormPanel2.this.reset ();
                    });
                }).build ());
            });
        }

        @Override
        public void process(Consumer<Optional<Object>> outcome) {
            if (validate ()) {
                // Perform the save action here.
                NotificationDialog.alert("Saving information", "The information has been saved", t -> {
                    // Object is used as a proxy. Normally we would return a representation of the
                    // updated object that can be used to propagate state changes.
                    outcome.accept (Optional.of (new Object ()));
                });
            } else
                outcome.accept (Optional.empty ());
        }
        
    }

    public static class SimpleFormPanel3 extends ControlForm<Void,Void> implements IProcessable<Object> {

        /************************************************************************
         * Dialog support.
         ************************************************************************/

        /**
         * See {@link #open(Consumer)}.
         */
        private static IDialogOpener<Void, Object> DIALOG;

        /**
         * Opens an instance of the panel in a dialog.
         * 
         * @param cb
         *           the callback.
         */
        public static void open(Consumer<Optional<Object>> cb) {
            if (DIALOG == null)
                DIALOG = ModalDialogCreator.<Void, Object, SimpleFormPanel3>dialog (new SimpleFormPanel3 (), cfg -> {
                    cfg.style (ModalStyle.UNIFORM)
                        .title ("Create project")
                        .type (Type.CENTER)
                        .width (Length.px(500));
                }, b -> b.label ("cancel"), b -> b.label ("Create project"));
            DIALOG.open (null, cb);
        }

        /************************************************************************
         * Construction.
         ************************************************************************/

        private SelectionGroupControl<String> privacyCtl;

        public SimpleFormPanel3() {
            super (ControlFormCreator.createForDialog ());
    
            group (grp -> {
                grp.control ("name", "Name of project", Controls.text (cfg -> {
                    cfg.validator (Validators.notEmpty ("please enter the name of the project"));
                }), cell -> cell.grow (1).required ());

            });
            group (grp -> {
                grp.control ("privacy", "Privacy", privacyCtl = Controls.radioGroup(cfg -> {
                    cfg.option ("option1", FontAwesome.users (), "Public to organisation", null)
                        .option ("option2", FontAwesome.lock (), "Private to me", null)
                        .validator (Validators.notEmpty ("please select an option"));
                }), cell -> cell.required ());
            });
            privacyCtl.setValue (ListSupport.list ("option1"));
        }

        @Override
        public void process(Consumer<Optional<Object>> outcome) {
            if (validate ()) {
                // Perform the save action here.
                NotificationDialog.alert("Saving information", "The information has been saved", t -> {
                    // Object is used as a proxy. Normally we would return a representation of the
                    // updated object that can be used to propagate state changes.
                    outcome.accept (Optional.of (new Object ()));
                });
            } else
                outcome.accept (Optional.empty ());
        }
        
    }
}
