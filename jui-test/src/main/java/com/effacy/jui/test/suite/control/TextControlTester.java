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
package com.effacy.jui.test.suite.control;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IAssignable;
import com.effacy.jui.test.TextValidator;
import com.effacy.jui.test.IPage.INode;

public class TextControlTester extends ControlTester<TextControlTester> implements IAssignable<TextControlTester> {
    
    public static TextControlTester $(String testId) {
        return new TextControlTester (testId);
    }

    protected INode inputEl;

    protected INode clearEl;

    public TextControlTester(String testId) {
        super (testId, "textcontrol");
        
    }

    public void resolve() {
        super.resolve();

        // Resolve the input.
        inputEl = el.selectByRef ("input");
        Assertions.assertNotNull (inputEl, "Unable to find test control input");

        // Resolve the clear action. This is optional.
        clearEl = el.selectByRef ("clear");
    }

    @Override
    public TextControlTester assign(String value) {
        inputEl.assignValue (value);
        return this;
    }

    /**
     * Click on the clear action of the control.
     * 
     * @return this instance.
     */
    public TextControlTester clickClear() {
        try {
            if (this.clearEl == null)
                Assertions.fail ("Clear action not enabled on text control [test-id=\"" + page.resolveTestId (testId) + "\"]");
            this.clearEl.click ();
        } catch (Exception e) {
            Assertions.fail ("Problem clicking on clear action", e);
        }
        return this;
    }
    
    /**
     * Validates the value of the input field of the control.
     * 
     * @param value the value of the input field.
     * @return this tester.
     */
    public TextControlTester validateInput(String value) {
        validate (new TextValidator(()->inputEl.value(), value));
        return this;
    }
}
