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
package com.effacy.jui.playground.it.ui.control;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.effacy.jui.playground.TestApplicationIT;
import com.effacy.jui.playground.it.AbstractIT;
import com.effacy.jui.test.PageTester;
import com.effacy.jui.test.suite.button.ButtonTester;
import com.effacy.jui.test.suite.navigation.TabNavigatorTester;

@SpringBootTest(classes = TestApplicationIT.class)
public class ControlITTest extends AbstractIT {
    /**
     * Tests a successful login and display of the profile.
     */
    @Test
    public void testNavigation() throws Exception {
        PageTester.$ (webClient, "http://localhost/playground?test=true", 4000) //

            // Grab the top-level tabset and activate "controls"
            .with (TabNavigatorTester.$ ("applicationui").subclass (), tabs -> {
                    tabs.validateTabs ("themes", "samples", "controls", "gallery", "editor", "dialogs");
                    tabs.validateActiveTab ("themes");
                    tabs.activate ("controls");
                    tabs.validateActiveTab ("controls");

                    // Under controls grab the add button and click to open the add dialog.
                    tabs.with (ButtonTester.$ ("controls.controlsection.controlsectiongroup.button"), btn -> {
                        btn.validateLabel ("Add");
                        btn.click ();
                    });
            }) //

            // Grab the dialog that has been opened above.
            .modal ("controlpanel001_dialog", dialog -> {
                dialog.validateTitle ("Create project");

                // Click on the create button with no content.
                dialog.button ("btn_create_project", btn -> btn.validateLabel ("Create project").click ());

                // Perform checks on the form controls.
                dialog.flow ("controlpanel01.controlsection", form -> {
                    form.textControl ("controlsectiongroup.name", ctl -> {
                        ctl.validateInput ("");
                        ctl.validateNotReadOnly ();
                        ctl.validateInvalid ();
                        ctl.field (field -> {
                            field.validateLabel ("Name of project");
                            field.validateError ("please enter the name of the project");
                        });
                    });
                });
            }) //

            // Print the page.
            .print (() -> {
                try {
                    return Files.createTempFile (null, ".html").toFile ();
                } catch (Throwable e) {
                    return null;
                }
            });
    }

}
