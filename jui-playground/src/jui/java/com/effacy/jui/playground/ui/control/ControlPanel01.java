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
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.ui.client.InfoBlock;
import com.effacy.jui.ui.client.control.SelectionGroupControl;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.modal.Modal.Type;
import com.effacy.jui.ui.client.modal.ModalDialog.Config.ModalStyle;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.modal.ModalDialogCreator.IDialogOpener;

public class ControlPanel01 extends ControlForm<Void,Void> implements IProcessable<Long> {

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
            DIALOG = ModalDialogCreator.<Void, Long, ControlPanel01>dialog (new ControlPanel01 (), cfg -> {
                cfg.style (ModalStyle.UNIFORM) 
                    .testId("controlpanel001_dialog") 
                    .title("Create project") 
                    .type(Type.CENTER) 
                    .width(Length.px(500));
            }, b -> b.label ("cancel"), b -> b.label ("Create project"));
        DIALOG.open (null, cb);
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    private SelectionGroupControl<String> privacyCtl;

    private InfoBlock<Void> messageBlk;

    /**
     * Construct an instane of the form panel.
     */
    public ControlPanel01() {
        super (ControlFormCreator.createForDialog());

        // ControlSection sec1 = section ().build ();
        // sec1.group (grp -> {
        //     grp.control (0, 0, new TextControl.Config () 
        //         .testId ("name") 
        //         .acceptor ("name") 
        //         .validator (NotEmptyValidator.validator ("please enter the name of the project")) 
        //         .build () 
        //     ).label ("Name of project").grow (1).required ();
        // });
        // sec1.group (grp -> {
        //     grp.control (0, 0, privacyCtl = new SelectionGroupControl.Config<String> ()
        //         .testId ("options")
        //         .radio ()
        //         .option ("option1", FontAwesome.users (), "Public to organisation", null)
        //         .option ("option2", FontAwesome.lock (), "Private to me", null)
        //         .acceptor ("privacy")
        //         .validator (NotEmptyValidator.validator ("please select an option"))
        //         .build ()
        //     ).label ("Privacy").required ();
        // });
        // sec1.group (grp -> {
        //     grp.gap (Length.em (1));
        //     grp.control (0, 0, new CheckControl.Config ()
        //         .modifiedHandler ((ctl, val, pri) -> {
        //             Logger.log ("V: " + val);
        //         })
        //         .testId ("template")
        //         .label ("Use project as template").labelBold ()
        //         .slider ().left ().expand ()
        //         .build ()
        //     ).grow (1);
        //     grp.control (1, 0, new CheckControl.Config ()
        //         .testId ("guideline")
        //         .label ("Use project as a guideline").labelBold ()
        //         .slider ().left ().expand ()
        //         .build ()
        //     ).grow (1);
        //     grp.component (2, 0, messageBlk = new InfoBlock.Config<Void> (renderer -> {
        //         renderer.line (line -> {
        //             line.item ().content ("Turn on to make the project a template or a shareable guideline.");
        //         });
        //     }).build ()).grow (1);
        // }, VertLayout.data ().separator (Separator.LINE).spacingAbove (Length.em (1.5)));
        // sec1.group (grp -> {
        //     grp.header ("Access rights");
        //     grp.gap (Length.em (1));
        //     grp.control (0, 0, new MultiCheckControl.Config<Grant> ()
        //         .testId ("user_groups_rights")
        //         .option (Grant.values (), null)
        //         .label ("User groups and rights").labelBold ()
        //         .left ().expand ().span (Length.em (13))
        //         .build ()
        //     ).grow (1);
        //     grp.control (1, 0, new MultiCheckControl.Config<Grant> ()
        //         .testId ("user_accounts")
        //         .option (Grant.values (), null)
        //         .label ("Users and accounts").labelBold ()
        //         .left ().expand ().span (Length.em (13))
        //         .build ()
        //     ).grow (1);
        // }, VertLayout.data ().separator (Separator.LINE).spacingAbove (Length.em (1.5)));

        messageBlk.update (null);
    }

    @Override
    public void reset() {
        super.reset ();
        privacyCtl.setValue (ListSupport.list ("option1"));
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
