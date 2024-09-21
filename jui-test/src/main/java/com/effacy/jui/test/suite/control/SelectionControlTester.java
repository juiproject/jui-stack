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

import com.effacy.jui.test.IPage.INode;

public class SelectionControlTester extends ControlTester<SelectionControlTester> {
    
    public static SelectionControlTester $(String testId) {
        return new SelectionControlTester (testId);
    }

    protected INode inputEl;

    protected INode removeEl;

    public SelectionControlTester(String testId) {
        super (testId, "selectioncontrol");
        
    }

    public void resolve() {
        super.resolve();

        // Resolve the input.
        inputEl = el.selectByRef ("input");
        Assertions.assertNotNull (inputEl, "Unable to find test control input");

        // Resolve the clear action. This is optional.
        removeEl = el.selectByRef ("remove");
    }

    /**
     * Click on the clear action of the control.
     * 
     * @return this instance.
     */
    public SelectionControlTester clickRemove() {
        try {
            if (this.removeEl == null)
                Assertions.fail ("Clear action not enabled on selection control [test-id=\"" + page.resolveTestId (testId) + "\"]");
            this.removeEl.click ();
        } catch (Exception e) {
            Assertions.fail ("Problem clicking on clear action", e);
        }
        return this;
    }

    public SelectionControlTester open() {
        try {
            this.inputEl.click ();
        } catch (Exception e) {
            Assertions.fail ("Problem clicking on selector input", e);
        }
        return this;
    }

    public SelectionControlTester select(int idx) {
        INode item = this.el.selectByRef ("item-" + idx);
        Assertions.assertNotNull (item, "Unable to find item of index " + idx + " for selection control [test-id=\"" + page.resolveTestId (testId) + "\"]");
        try {
            item.click ();
        } catch (Exception e) {
            Assertions.fail ("Problem clicking on selector item " + idx, e);
        }
        return this;
    }
}
