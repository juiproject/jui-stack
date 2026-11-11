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
package com.effacy.jui.playground.ui.testing;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.StateComponentCreator;
import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.state.ValueStateVariable;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;

/**
 * Buttons with and without options (see the playground IT
 * {@code ButtonOptionsITTest}): a plain button, a split button (with controls
 * to disable and enable it), a menu button, a split button in a container that
 * clips its overflow and one in a transformed container. Each reports what ran
 * to a status line, and an option also reports whether the menu was closed when
 * its handler ran.
 */
public class ButtonSuite extends Panel {

    private ValueStateVariable<String> status = new ValueStateVariable<> ("none");

    private int plainClicks;

    public ButtonSuite() {
        super (PanelCreator.config ().scrollable ().padding (Insets.em (2)).layout (VertLayoutCreator.create (Length.em (1))));

        add (StateComponentCreator.build (status, (s, el) -> {
            Div.$ (el).testId ("buttons.status").text (s.value ());
        }));

        // Without options (built and behaving as always).
        add (ButtonCreator.build (cfg -> {
            cfg.testId ("buttons.plain");
            cfg.label ("Plain");
            cfg.icon (FontAwesome.person ());
            cfg.handler (() -> status.assign ("plain:" + (++plainClicks)));
        }));

        // Split button.
        Button[] split = new Button[1];
        split[0] = add (ButtonCreator.build (cfg -> {
            cfg.testId ("buttons.split");
            cfg.label ("Add requirement");
            cfg.icon (FontAwesome.plus ());
            cfg.split ();
            cfg.handler (() -> status.assign ("split:main"));
            options (cfg, "split", split);
        }));
        add (ComponentCreator.build (root -> {
            Btn.$ (root, "Disable split").testId ("buttons.disable").onclick (() -> split[0].disable ());
            Btn.$ (root, "Enable split").testId ("buttons.enable").onclick (() -> split[0].enable ());
        }));

        // Menu button (its own handler is not used).
        Button[] menu = new Button[1];
        menu[0] = add (ButtonCreator.build (cfg -> {
            cfg.testId ("buttons.menu");
            cfg.label ("Add");
            cfg.variant (Button.Config.Variant.OUTLINED);
            cfg.handler (() -> status.assign ("menu:handler"));
            options (cfg, "menu", menu);
        }));

        // In a container that clips its overflow.
        Button[] clipped = new Button[1];
        add (ComponentCreator.build (root -> {
            Div.$ (root).testId ("buttons.clip").css ("overflow: hidden; height: 3em; width: 24em; padding: 0.25em; border: 1px dashed var(--jui-role-border-default);").$ (box -> {
                clipped[0] = ButtonCreator.$ (box, cfg -> {
                    cfg.testId ("buttons.clipped");
                    cfg.label ("Add requirement");
                    cfg.variant (Button.Config.Variant.OUTLINED);
                    cfg.split ();
                    cfg.handler (() -> status.assign ("clipped:main"));
                    options (cfg, "clipped", clipped);
                });
            });
        }));

        // In a transformed container (which places fixed elements against itself).
        Button[] transformed = new Button[1];
        add (ComponentCreator.build (root -> {
            Div.$ (root).testId ("buttons.transform").css ("transform: translate(3em, 0); width: 24em; padding: 0.25em; border: 1px dashed var(--jui-role-border-default);").$ (box -> {
                transformed[0] = ButtonCreator.$ (box, cfg -> {
                    cfg.testId ("buttons.transformed");
                    cfg.label ("Add");
                    cfg.split ();
                    cfg.handler (() -> status.assign ("transformed:main"));
                    options (cfg, "transformed", transformed);
                });
            });
        }));
    }

    /**
     * Adds the options (each reports to the status line, with whether the menu
     * was closed when it ran).
     */
    private void options(Button.Config cfg, String name, Button[] btn) {
        cfg.option ("Requirement", "Something the system must do", "REQ", () -> chosen (name, "REQ", btn[0]));
        cfg.option ("Rule", "A rule that governs the requirements", "RULE", () -> chosen (name, "RULE", btn[0]));
        cfg.option ("Constraint", null, "CON", () -> chosen (name, "CON", btn[0]));
        cfg.iconOption ("Non-functional requirement", "How well the system must do it", FontAwesome.gauge (), () -> chosen (name, "NFR", btn[0]));
    }

    private void chosen(String name, String option, Button btn) {
        status.assign (name + ":" + option + ((btn.isOptionsOpen ()) ? " open" : " closed"));
    }
}
