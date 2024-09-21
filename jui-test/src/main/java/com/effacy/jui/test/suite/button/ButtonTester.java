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
package com.effacy.jui.test.suite.button;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IClickable;
import com.effacy.jui.test.TextValidator;
import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.component.ComponentTester;

/**
 * For testing {@link Button}'s.
 */
public class ButtonTester extends ComponentTester<ButtonTester> implements IClickable<ButtonTester> {

    /**
     * Construct tester instance.
     * 
     * @param testId the test ID to bind to.
     * @return the instance.
     */
    public static ButtonTester $(String testId) {
        return new ButtonTester (testId);
    }

    /**
     * The anchor element.
     */
    protected INode anchorEl;

    /**
     * The label container element.
     */
    protected INode labelEl;

    /**
     * Construct instance of tester.
     * 
     * @param testId the test ID to bind to.
     */
    public ButtonTester(String testId) {
        super (testId, "button");
        
    }

    @Override
    public void resolve() {
        super.resolve();

        this.anchorEl = el.selectByRef ("action");
        Assertions.assertNotNull(this.anchorEl, "Unable to find button anchor");

        this.labelEl = el.selectByRef ("label");
        Assertions.assertNotNull(this.labelEl, "Unable to find button label");
    }

    /**
     * Invokes a click on the button.
     * 
     * @param delay the post-click delay in milliseconds.
     */
    public ButtonTester click(long delay) {
        try {
            anchorEl.click ();
            sleep (delay);
        } catch (Exception e) {
            Assertions.fail("Failed to click button [test-id=\"" + testId + "\"]", e);
        }
        return this;
    }

    /**
     * Validates the value of the label.
     * 
     * @param label the expected label.
     * @return this tester.
     */
    public ButtonTester validateLabel(String label) {
        validate (new TextValidator (() -> labelEl.textContent (), label));
        return this;
    }


}
