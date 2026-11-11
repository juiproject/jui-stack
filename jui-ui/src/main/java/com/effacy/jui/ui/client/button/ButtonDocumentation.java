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
package com.effacy.jui.ui.client.button;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.button.Button.Config.Behaviour;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.DomGlobal;

public class ButtonDocumentation {
    
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
        .title ("Button")
        .className (Button.class.getCanonicalName())
        .description ("A customisable button with a separate action state as well as standard states.")
        .example (ButtonCreator.config ().label ("Hello").icon (FontAwesome.cloudArrowDown ()).handler (cb -> {
            DomGlobal.window.alert ("Hello!");
            cb.complete ();
        }).build ())
        .option ("label", option-> {
            option.description ("The display label for the button");
            option.required ();
            option.valueType ("string");
        })
        .option ("icon", option -> {
            option.description ("An icon to display for the button");
            option.valueType ("string");
            option.valueDescription ("This should be a standard space-separated list of CSS styles that effect the display of the icon (for example, FontAwesome).");
        })
        .option("iconOnRight", option -> {
            option.description ("When there is an icon display to the right of the label (default is the left)");
            option.valueType ("boolean");
        })
        .option ("behaviour", option -> {
            option.description ("The behaviour of the button when clicked");
            option.required ();
            option.valueType ("enum{Behaviour}");
            option.value (Button.Config.Behaviour.DISABLE, "Disable the button until the handler callback returns");
            option.value (Button.Config.Behaviour.NORMAL, "No change in visual state");
            option.value (Button.Config.Behaviour.WAIT, "Display a waiting state (with a visual spinner) until the handler callback returns");
        })
        .option ("option", option -> {
            option.description ("Adds an option (label, a line saying what it is, a short code shown before the label and a handler). Options show in a menu beneath the button in the order added; choosing one closes the menu then runs its handler. Without split() the whole button opens the menu (and its own handler is not used).");
            option.valueType ("(String,String,String,Invoker)");
        })
        .option ("iconOption", option -> {
            option.description ("As option but with an icon in place of the code.");
            option.valueType ("(String,String,String,Invoker)");
        })
        .option ("split", option -> {
            option.description ("With options, the button keeps its own handler as its action and a chevron beside it (divided by a hairline) opens the options.");
            option.valueType ("boolean");
        })
        .style ("LINK", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A simple version of the button that displays as a link rather than a formed button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("OUTLINED", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A standard button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A standard button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_SUCCESS", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A standard button coloured to indicate a success path.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_WARNING", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A standard button coloured to indicate a state of wariness.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_DANGER", style -> {
            style.css ("Button.css","Button_Override.css");
            style.description("A standard button coloured to indicate a state of alertness.");
            style.variable ("*", "Common theme variables");
        })    
        .style ("-", style -> {
            style.description("Theme variables common to all button styles");
            style.variable ("--btn-color-primary", "Primary button color");
            style.variable ("--btn-color-hover", "Button hover color");
            style.variable ("--btn-color-outline", "Outline color");
        })
        .example(ButtonCreator.config ().testId("btn-1").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL).behaviour (Behaviour.WAIT).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
        }).build (), detail -> {
            detail.description ("Normal button with a spinner when activated.");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .style (Button.Config.Style.NORMAL)",
                "  .behaviour (Behaviour.WAIT)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-2").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL).behaviour (Behaviour.DISABLE).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
        }).build (), detail -> {
            detail.description ("Normal button that disables when activated.");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .style (Button.Config.Style.NORMAL)",
                "  .behaviour (Behaviour.DISABLE)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-3").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL).behaviour (Behaviour.NORMAL).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
        }).build (), detail -> {
            detail.description ("Normal button that does not change state when pressed.");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .style (Button.Config.Style.NORMAL)",
                "  .behaviour (Behaviour.NORMAL)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(() -> {
            Button button = ButtonCreator.build (cfg -> {
                cfg.testId("btn-4");
                cfg.label ("A button");
                cfg.icon (FontAwesome.person());
                cfg.style (Button.Config.Style.NORMAL);
                cfg.behaviour (Behaviour.NORMAL);
            });
            button.disable ();
            return button;
        }, detail -> {
            detail.description ("Normal button (disabled)");
            detail.code (StringSupport.compose (
                "Button button = ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .style (Button.Config.Style.NORMAL)",
                "  .behaviour (Behaviour.NORMAL)", 
                "  .build ();",
                "button.disable ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-5").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL_DANGER).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
            cb.complete ();
        }).build (), detail -> {
            detail.description ("Danger button");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .behaviour (Behaviour.NORMAL_DANGER)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-6").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL_SUCCESS).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
            cb.complete ();
        }).build (), detail -> {
            detail.description ("Success button");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .behaviour (Behaviour.NORMAL_SUCCESS)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-7").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.NORMAL_WARNING).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
            cb.complete ();
        }).build (), detail -> {
            detail.description ("Warning button");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .behaviour (Behaviour.NORMAL_WARNING)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-8").label ("A button").icon (FontAwesome.person()).style (Button.Config.Style.OUTLINED).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
            cb.complete ();
        }).build (), detail -> {
            detail.description ("Outlined button");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"A button\")",
                "  .icon (FontAwesome.person ())",
                "  .behaviour (Behaviour.OUTLINED)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(ButtonCreator.config ().testId("btn-9").label ("a button").icon (FontAwesome.person()).style (Button.Config.Style.LINK).handler (cb -> {
            NotificationDialog.alert ("Button pressed", "The button was pressed.", t -> cb.complete());
        }).build (), detail -> {
            detail.description ("Link button");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"a button\")",
                "  .icon (FontAwesome.person ())",
                "  .behaviour (Behaviour.LINK)", 
                "  .handler (cb -> NotificationDialog.alert (",
                "    \"Button pressed\",",
                "    \"The button was pressed.\",",
                "    () -> cb.complete()",
                "  ))",
                "  .build ();"
            ));
        })
        .example(() -> {
            Button button = ButtonCreator.build (cfg -> {
                cfg.testId("btn-10");
                cfg.label ("a button");
                cfg.icon (FontAwesome.person());
                cfg.style (Button.Config.Style.LINK);
            });
            button.disable ();
            return button;
        },  detail -> {
            detail.description ("Link button (disabled)");
            detail.code (StringSupport.compose (
                "Button button = ButtonCreator.config ()",
                "  .label (\"a button\")",
                "  .icon (FontAwesome.person ())",
                "  .style (Button.Config.Style.LINK)",
                "  .build ();",
                "button.disable ();"
            ));
        })
        .example (ButtonCreator.config ().testId ("btn-11").label ("Add requirement").icon (FontAwesome.plus ()).split ()
            .handler (() -> NotificationDialog.alert ("Add requirement", "The button's own action.", t -> {}))
            .option ("Requirement", "Something the system must do", "REQ", () -> NotificationDialog.alert ("Option chosen", "Requirement", t -> {}))
            .option ("Rule", "A rule that governs the requirements", "RULE", () -> NotificationDialog.alert ("Option chosen", "Rule", t -> {}))
            .option ("Constraint", null, "CON", () -> NotificationDialog.alert ("Option chosen", "Constraint", t -> {}))
            .iconOption ("Non-functional requirement", "How well the system must do it", FontAwesome.gauge (), () -> NotificationDialog.alert ("Option chosen", "Non-functional requirement", t -> {}))
            .build (), detail -> {
            detail.description ("Split button: the button runs its own action and the chevron opens the options.");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"Add requirement\")",
                "  .icon (FontAwesome.plus ())",
                "  .split ()",
                "  .handler (() -> ...)",
                "  .option (\"Requirement\", \"Something the system must do\", \"REQ\", () -> ...)",
                "  .option (\"Rule\", \"A rule that governs the requirements\", \"RULE\", () -> ...)",
                "  .option (\"Constraint\", null, \"CON\", () -> ...)",
                "  .iconOption (\"Non-functional requirement\", \"How well the system must do it\", FontAwesome.gauge (), () -> ...)",
                "  .build ();"
            ));
        })
        .example (ButtonCreator.config ().testId ("btn-12").label ("Add").variant (Button.Config.Variant.OUTLINED)
            .option ("Requirement", "Something the system must do", "REQ", () -> NotificationDialog.alert ("Option chosen", "Requirement", t -> {}))
            .option ("Rule", "A rule that governs the requirements", "RULE", () -> NotificationDialog.alert ("Option chosen", "Rule", t -> {}))
            .build (), detail -> {
            detail.description ("Menu button: the whole button opens the options (the button's own handler is not used).");
            detail.code (StringSupport.compose (
                "ButtonCreator.config ()",
                "  .label (\"Add\")",
                "  .variant (Button.Config.Variant.OUTLINED)",
                "  .option (\"Requirement\", \"Something the system must do\", \"REQ\", () -> ...)",
                "  .option (\"Rule\", \"A rule that governs the requirements\", \"RULE\", () -> ...)",
                "  .build ();"
            ));
        })
        .example (() -> ComponentCreator.build (root -> {
            Div.$ (root).css ("overflow: hidden; height: 3em; padding: 0.25em; border: 1px dashed var(--jui-role-border-default);").$ (box -> {
                ButtonCreator.$ (box, cfg -> cfg.testId ("btn-13").label ("Add").split ()
                    .handler (() -> NotificationDialog.alert ("Add", "The button's own action.", t -> {}))
                    .option ("Requirement", "Something the system must do", "REQ", () -> NotificationDialog.alert ("Option chosen", "Requirement", t -> {}))
                    .option ("Rule", "A rule that governs the requirements", "RULE", () -> NotificationDialog.alert ("Option chosen", "Rule", t -> {})));
            });
        }), detail -> {
            detail.description ("Split button in a container that clips its overflow (outlined): the menu is placed against the window so is not clipped.");
            detail.code (StringSupport.compose (
                "Div.$ (root).css (\"overflow: hidden; height: 3em;\").$ (box -> {",
                "  ButtonCreator.$ (box, cfg -> cfg.label (\"Add\").split ()",
                "    .handler (() -> ...)",
                "    .option (\"Requirement\", \"Something the system must do\", \"REQ\", () -> ...)",
                "    .option (\"Rule\", \"A rule that governs the requirements\", \"RULE\", () -> ...));",
                "});"
            ));
        })
        .build ();
    }
}
