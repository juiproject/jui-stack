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
        .style ("LINK", style -> {
            style.css ("Button_Link.css","Button_Link_Override.css");
            style.description("A simple version of the button that displays as a link rather than a formed button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("OUTLINED", style -> {
            style.css ("Button_Outlined.css","Button_Outlined_Override.css");
            style.description("A standard button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL", style -> {
            style.css ("Button_Normal.css","Button_Normal_Override.css");
            style.description("A standard button.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_SUCCESS", style -> {
            style.css ("Button_NormalSuccess.css","Button_NormalSuccess_Override.css");
            style.description("A standard button coloured to indicate a success path.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_WARNING", style -> {
            style.css ("Button_NormalWarning.css","Button_NormalWarning_Override.css");
            style.description("A standard button coloured to indicate a state of wariness.");
            style.variable ("*", "Common theme variables");
        })
        .style ("NORMAL_DANGER", style -> {
            style.css ("Button_NormalDanger.css","Button_NormalDanger_Override.css");
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
        .build ();
    }
}
