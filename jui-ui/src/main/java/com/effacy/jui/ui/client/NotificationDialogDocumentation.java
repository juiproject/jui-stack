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
package com.effacy.jui.ui.client;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.NotificationDialog.OutcomeType;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.icon.FontAwesome;

public class NotificationDialogDocumentation {
    
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
        .title ("NotificationDialog")
        .className (NotificationDialog.class.getCanonicalName())
        .description ("An out-of-the-box solution for simple notification dialogs (for comfirm, alerts and general notifications).")
        .example (ButtonCreator.config ().label ("Confirm").icon (FontAwesome.check ()).handler (cb -> {
            NotificationDialog.confirm ("Please confirm")
                .notice ("Are you sure you want to confirm?")
                .handler (outcome -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    cb.complete ();
                })
                .open ();
        }).build ())
        .example (ButtonCreator.config ().testId("btn-1").label ("Show confirmation").style (Button.Config.Style.NORMAL).handler (cb -> {
            NotificationDialog.confirm ("Confirmation dialog")
                .notice ("This is a confirmation dialog")
                .handler (outcome -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    cb.complete ();
                })
                .open ();
        }).build (), detail -> {
            detail.description ("A confirmation dialog.");
            detail.code (StringSupport.compose (
                "NotificationDialog.confirm (\"Confirmation dialog\")",
                "  .notice (\"This is a confirmation dialog\")",
                "  .open ();",
                "",
                "/* OR */",
                "",
                "NotificationDialog.confirm (",
                "  \"Confirmation dialog\",",
                "  \"This is a confirmation dialog\",",
                "  null",
                ");"
            ));
        })
        .example (ButtonCreator.config ().testId("btn-1").label ("Show alert").style (Button.Config.Style.NORMAL_WARNING).handler (cb -> {
            NotificationDialog.alert ("Alert dialog")
                .notice ("This is an alert dialog")
                .handler (outcome -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    cb.complete ();
                })
                .open ();
        }).build (), detail -> {
            detail.description ("An alert dialog.");
            detail.code (StringSupport.compose (
                "NotificationDialog.alert (\"Alert dialog\")",
                "  .notice (\"This is an alert dialog\")",
                "  .open ();"
            ));
        })
        .example (ButtonCreator.config ().testId("btn-1").label ("Show error").style (Button.Config.Style.NORMAL_DANGER).handler (cb -> {
            NotificationDialog.error ("Error dialog")
                .notice ("This is an error dialog")
                .handler (outcome -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    cb.complete ();
                })
                .open ();
        }).build (), detail -> {
            detail.description ("An error dialog.");
            detail.code (StringSupport.compose (
                "NotificationDialog.error (\"Error dialog\")",
                "  .notice (\"This is an error dialog\")",
                "  .open ();"
            ));
        })
        .example (ButtonCreator.config ().testId("btn-1").label ("Show save").style (Button.Config.Style.NORMAL_SUCCESS).handler (cb -> {
            NotificationDialog.save ("Save dialog")
                .notice ("This is a save dialog")
                .handler (outcome -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    cb.complete ();
                })
                .open ();
        }).build (), detail -> {
            detail.description ("A save dialog. This includes an additional action to disard the changes resulting in a discard outcome.");
            detail.code (StringSupport.compose (
                "NotificationDialog.save (\"Save dialog\")",
                "  .notice (\"This is a save dialog\")",
                "  .handler (outcome -> {",
                "    Logger.log (\"Outcome: \" + outcome.name ());",
                "  })",
                "  .open ();"
            ));
        })
        .example (ButtonCreator.config ().testId("btn-1").label ("Long running action").style (Button.Config.Style.NORMAL).handler (cb -> {
            NotificationDialog.confirm ("Confirmation action")
                .notice ("Are you wanting to perform this action?")
                .handler ((outcome,done) -> {
                    Logger.log ("Outcome: " + outcome.name ());
                    if (OutcomeType.OK == outcome) {
                        TimerSupport.timer(() -> {
                            done.complete ();
                        }, 2000);
                    } else
                        done.complete ();
                })
                .close (() -> cb.complete())
                .open ();
        }).build (), detail -> {
            detail.description ("Mimics a long running action (about 2 seconds) when confirming. The dialog will remain until the action is complete.");
            detail.code (StringSupport.compose (
                "NotificationDialog.save (\"Confirmation action\")",
                "  .notice (\"Are you wanting to perform this action?\")",
                "  .handler ((outcome,done) -> {",
                "    Logger.log (\"Outcome: \" + outcome.name ());",
                "    if (OutcomeType.OK == outcome) {",
                "      /* async operations */.onComplete (() -> {",
                "        done.complete ();",
                "      });",
                "    } else",
                "      done.complete ();",
                "  })",
                "  .open ();"
            ));
        })
        .build ();
    }
}
