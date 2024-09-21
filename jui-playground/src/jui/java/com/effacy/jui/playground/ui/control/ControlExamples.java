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

import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;

/**
 * ControlExample
 *
 * @author Jeremy Buckley
 */
public class ControlExamples extends ControlForm<Void,Void> {

    TextControl middleName;

    public ControlExamples() {
        super (ControlFormCreator.create());

        group (sec -> {
            sec.header (cfg -> {
                cfg.title ("Section title");
            }); 
            sec.group (grp -> {
                // grp.control (0, 0, "First name", TextControlCreator.build (cfg -> cfg.placeholder ("First name"))).grow (1);
                // grp.control (0, 1, "Middle name(s)", middleName = TextControlCreator.build (cfg -> cfg.readOnly ())).grow (1);
                // grp.control (0, 2, "Last name", TextControlCreator.build (cfg -> cfg.clearAction (true))).grow (1);
                // grp.control (1, 0, "Password", TextControlCreator.build (cfg -> cfg.placeholder ("Password").password ())).grow (1);
                // grp.component (1, 1, ButtonCreator.build (cfg -> cfg.style (Button.Config.Style.NORMAL).label ("Add").icon (FontAwesome.plus (FontAwesome.Option.BOLD)).handler (cb -> {
                //     ControlPanel01.open (outcome -> {
                //         cb.complete ();
                //     });
                // })));
                // grp.control (2, 0, "Description", TextAreaControlCreator.build (cfg -> cfg.rows (10)
                //     .modifiedHandler ((ctl, v, p) -> {
                //         Logger.log ("Modified: " + v);
                //     }))).grow (1);
                // grp.control (3, 0, "Option", SelectionControlCreator.<String> build (cfg -> cfg
                //     .selectorLeft (false)
                //     .allowEmpty (true)
                //     .selectorHeight (Length.em (13))
                //     .store (new ListStore<String> ()
                //         .add ("Option 1")
                //         .add ("Option 2")
                //         .add ("Option 3")
                //         .add ("Option 4")
                //         .add ("Option 5")
                //         .add ("Option 6")
                //         .add ("Option 7")
                //         .add ("Option 8")
                // ))).width (Length.em (20)).grow (1);
                // grp.control (4, 0, "Configuration", SelectionGroupControlCreator.<String> build (cfg -> cfg
                //     .radio (false)
                //     .modifiedHandler ((ctl, v, p) -> {
                //         Logger.log ("Modified: " + ListSupport.contract (v));
                //     })
                //     .option ("option1", FontAwesome.ambulance (), "The first option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                //     .option ("option2", FontAwesome.appleAlt (), "The second option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                //     .option ("option3", FontAwesome.archive (), "The third option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                //     .option ("option4", "The fourth option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                //     .option ("option5", "The fifth option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                //     .option ("option6", "The sixth option to choose", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                // ));
            });
        });
        //middleName.setValue ("Hubba");

        bar (bar -> {
            bar.add (ButtonCreator.config ().label ("Update").build (), ActionBarLayout.data (1));
        });
    }

}
