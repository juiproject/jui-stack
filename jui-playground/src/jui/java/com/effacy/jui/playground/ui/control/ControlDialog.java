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

import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IProcessable;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.playground.ui.Grant;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.SelectionGroupControl;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.Modal.Type;
import com.effacy.jui.ui.client.modal.ModalDialog.Config.ModalStyle;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.modal.ModalDialogCreator.IDialogOpener;
import com.effacy.jui.validation.model.validator.Validators;

public class ControlDialog extends ControlForm<Void,Void> implements IProcessable<Long> {

    /************************************************************************
     * Dialog support.
     ************************************************************************/

    /**
     * See {@link #open(Consumer)}.
     */
    private static IDialogOpener<Void, Long> DIALOG;

    /**
     * Opens an instance of the panel in a dialog.
     * 
     * @param cb
     *           the callback.
     */
    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<Void, Long, ControlDialog>dialog (new ControlDialog (), cfg -> {
                cfg.style (ModalStyle.UNIFORM) 
                    .testId("controldialog") 
                    .title("Create project") 
                    .type(Type.CENTER) 
                    .width(Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create project"));
        DIALOG.open (null, cb);
    }

    private SelectionGroupControl<Object> privacyCtl;
        
    public ControlDialog() {
        super (new ControlForm.Config ().style (ControlForm.Config.Style.COMPACT).startingDepth (1).padding (Insets.em (2.5, 2)));

        group (sec -> {
            sec.control ("name", "Name of project", Controls.text (cfg -> {
                cfg.testId ("name")
                    .acceptor ("name").validator (Validators.notEmpty ("please enter the name of the project"));
            }), handler -> handler.grow (1).required ());
            sec.control ("privacy", "Privacy", privacyCtl = Controls.radioGroup (cfg -> {
                cfg.testId ("options")
                    .option ("option1", FontAwesome.users (), "Public to organisation", null)
                    .option ("option2", FontAwesome.lock (), "Private to me", null)
                    .acceptor ("privacy").validator (Validators.notEmpty ("please select an option"));
            }), handler -> handler.required ());
        });
        group (sec -> {
            sec.separator ();
            sec.header (hdr -> hdr.title ("Project re-use"));
            sec.control ("template", "", Controls.check (cfg -> {
                cfg.testId ("template")
                    .label ("Use project as template")
                    .slider ().left ().expand ();
            }), handler -> {
                handler.grow (1);
                handler.modify ((v,ctl) -> {
                    Logger.log ("V: " + v);
                });
            });
            sec.control ("guideline", "", Controls.check (cfg -> {
                cfg.testId ("guideline")
                    .label ("Use project as a guideline")
                    .slider ().left ().expand ();
            }), handler -> {
                handler.grow (1);
                handler.modify ((v,ctl) -> {
                    Logger.log ("V: " + v);
                });
            });
        });
        group (sec -> {
            sec.separator ();
            sec.header (hdr -> hdr.title ("Access rights"));
            sec.control ("user_groups_rights", "", Controls.<Grant> checkMulti (cfg -> {
                cfg.testId ("user_groups_rights")
                    .option (Grant.values (), null)
                    .label ("User groups and rights")
                    .left ().expand ().span (Length.em (13));
            }), handler -> handler.grow (1));
            sec.control ("user_accounts", "", Controls.<Grant> checkMulti (cfg -> {
                cfg.testId ("user_accounts")
                    .option (Grant.values (), null)
                    .label ("Users and accounts")
                    .left ().expand ().span (Length.em (13));
            }), handler -> handler.grow (1));
        });
        handleReset (ctx -> {
            if (privacyCtl != null)
                privacyCtl.setValue (ListSupport.list ("option1"));
        });
        reset ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IProcessable#process(java.util.function.Consumer)
     */
    @Override
    public void process(Consumer<Optional<Long>> outcome) {
        if (!validate ()) {
            outcome.accept (Optional.empty ());
            return;
        }
        outcome.accept (Optional.of (1L));
    }
}
