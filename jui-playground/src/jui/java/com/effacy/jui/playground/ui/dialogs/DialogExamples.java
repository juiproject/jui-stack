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
package com.effacy.jui.playground.ui.dialogs;

import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.playground.ui.shared.TextComponent;
import com.effacy.jui.ui.client.button.Button.Config.Behaviour;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.modal.ModalDialog;
import com.effacy.jui.ui.client.modal.ModalDialog.IDialogActionHandler.ICallback;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.panel.Panel;

/**
 * Various examples of modals and dialogs.
 *
 * @author Jeremy Buckley
 */
public class DialogExamples extends Panel {

    public DialogExamples() {
        super (new Panel.Config().padding (Insets.em (2)).scrollable ().layout (VertLayout.$ ().separator (Separator.LINE).spacing (Length.em (1)).build ()));

        add (ButtonCreator.config ().label ("Dialog 1").handler (cb -> {
            new Dialog1 ().open ();
            cb.complete ();
        }).build ());

        add (ButtonCreator.config ().label ("Dialog 2").handler (cb -> {
            new Dialog2 ().open ();
            cb.complete ();
        }).build ());

        add (ButtonCreator.config ().behaviour (Behaviour.NORMAL).label ("Dialog 3").handler (cb -> {
            ModalDialogCreator.build (new TextComponent ("Hello, this is a dialog"), cfg -> {
                cfg.title ("Example dialog").width (Length.px (400)).closable ().removeOnClose ();
                cfg.action (a -> a.label ("Confirm").handler (ah -> ah.success ()));
            }).open ();
            cb.complete ();
        }).build ());

        add (ButtonCreator.config ().behaviour (Behaviour.NORMAL).label ("Dialog 4").handler (cb -> {
            ModalDialogCreator.build (ControlFormCreator.build (cfg -> {
                //cfg.messagesStyles (NotificationBlock.Config.Style.STANDARD_COMPACT).padding (Insets.em (2.5, 2));
            }, panel -> {
                // ControlSection sec1 = panel.section ().build ();
                // sec1.group (grp -> {
                //     grp.control (0, 0, TextControlCreator.create () //
                //             .name ("name") //
                //             .acceptor ("name") //
                //             .validator (NotEmptyValidator.validator ("please enter the name of the project")) //
                //             .build () //
                //     ).label ("Name of project").grow (1).required ();
                // });
                // sec1.group (grp -> {
                //     grp.control (0, 0, new SelectionGroupControl.Config<String> () //
                //             .name ("privacy") //
                //             .radio () //
                //             .option ("option1", FontAwesome.users (), "Public to organisation", null) //
                //             .option ("option2", FontAwesome.lock (), "Private to me", null) //
                //             .acceptor ("privacy") //
                //             .validator (NotEmptyValidator.validator ("please select an option")) //
                //             .build ()).label ("Privacy").required ();
                // });
            }), cfg -> {
                cfg.title ("Example form dialog").width (Length.px (400)).closable ().removeOnClose ();
                cfg.action (a -> a.label ("Create").handler (ah -> {
                    if (ah.contents ().validate ()) {
                        String name = (String) ah.contents ().value ("name");
                        String privacy = (String) ah.contents ().value ("privacy");
                        Logger.info ("name: " + name);
                        Logger.info ("privacy: " + privacy);
                        ah.success ();
                    } else
                        ah.fail ();
                }));
            }).open ();
            cb.complete ();
        }).build ());
    }

    /**
     * Demonstrates the use of configuration to shape the dialog and declare actions
     * with action handlers.
     */
    public static class Dialog1 extends ModalDialog<TextComponent> {

        public Dialog1() {
            super (new ModalDialog.Config<TextComponent> ()
                .title ("Example dialog")
                .width (Length.px (400))
                .closable ()
                .removeOnClose (),
                new TextComponent ("This is an example dialog")
            );
            config ().action (a -> a.label ("dismiss").link ().handler (cb -> {
                cb.success ();
            }));
            config ().action (a -> a.label ("Open another").handler (cb -> {
                new Dialog1 ().open ();
                cb.fail ();
            }));
        }

    }

    /**
     * Variation of {@link Dialog1} that makes use of action references rather than
     * handlers.
     */
    public static class Dialog2 extends ModalDialog<TextComponent> {

        public Dialog2() {
            super (new ModalDialog.Config<TextComponent> ()
                .title ("Example dialog") 
                .width (Length.px (400))
                .closable () 
                .removeOnClose (), 
                new TextComponent ("This is an example dialog") 
            );
            config ().action (a -> a.label ("dismiss").reference ("close").link ());
            config ().action (a -> a.label ("Open another").reference ("open"));
        }

        @Override
        protected void handleAction(Object reference, ICallback<TextComponent> cb) {
            if ("close".equals (reference)) {
                cb.success ();
            } else if ("open".equals (reference)) {
                new Dialog2 ().open ();
                cb.fail ();
            }
        }

    }
}
