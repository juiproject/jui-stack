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
package com.effacy.jui.test.modal;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.component.ComponentTester;

/**
 * Tester for a {@link ModalDialog}.
 */
public class ModalDialogTester extends ComponentTester<ModalDialogTester> {

    /**
     * Create instance of a dialog tester.
     * 
     * @param testId the test ID to match.
     * @return the dialog.
     */
    public static ModalDialogTester $(String testId) {
        return new ModalDialogTester(testId);
    }

    /**
     * The title element.
     */
    private INode titleEl;

    /**
     * The sub-title element.
     */
    private INode subtitleEl;

    /**
     * The close action element.
     */
    private INode closeEl;

    public ModalDialogTester(String testId) {
        super (testId, "modaldialog");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve() {
        super.resolve();

        this.titleEl = el.selectByRef ("dialog_title");
        Assertions.assertNotNull(this.titleEl, "Unable to find dialog title [test-d=\"" + page.resolveTestId (testId) + "\"]");

        this.subtitleEl = el.selectByRef ("dialog_subtitle");

        this.closeEl = el.selectByRef ("dialog_close");
    }
    
    /**
     * Validates the value of the title.
     * 
     * @param title the value to check.
     * @return this tester instance.
     */
    public ModalDialogTester validateTitle(String title) {
        validate  (() -> {
            Assertions.assertEquals(title, this.titleEl.textContent(), "Mismatched title for dialog [test-d=\"" + page.resolveTestId (testId) + "\"]");
        });
        return this;
    }
    
    /**
     * Validates the value of the sub-title.
     * 
     * @param subtitle the value to check.
     * @return this tester instance.
     */
    public ModalDialogTester validateSubtitle(String subtitle) {
        validate  (() -> {
            Assertions.assertNotNull (this.subtitleEl, "No subtitle for dialog [test-d=\"" + page.resolveTestId (testId) + "\"]");
            Assertions.assertEquals (subtitle, this.subtitleEl.textContent(), "Mismatched subtitle for dialog [test-d=\"" + page.resolveTestId (testId) + "\"]");
        });
        return this;
    }

    /**
     * Clicks on the close action.
     * 
     * @return this tester instance.
     */
    public ModalDialogTester clickClose () {
        Assertions.assertNotNull(closeEl, "Unable to find close action on dialog [test-d=\"" + page.resolveTestId (testId) + "\"]");
        try {
            closeEl.click ();
        } catch (Throwable e) {
            Assertions.fail("Problem closing dialog", e);
        }
        return this;
    }
}
