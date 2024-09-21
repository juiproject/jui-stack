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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.Tester;
import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.component.ComponentTester;
import com.effacy.jui.test.IResolvable;

public class ControlGroupFieldTester extends Tester<ControlGroupFieldTester> {

    private String testId;

    protected INode colEl;

    protected INode rowEl;

    protected INode cellEl;

    protected INode labelEl;

    protected INode errorsEl;

    public ControlGroupFieldTester(String testId) {
        this.testId = testId;
    }

    /**
     * Constructs from a node. This is the same as
     * {@link #ControlGroupFieldTester(String)} where it obtains the ID from the
     * node.
     * 
     * @param tester the node to use.
     */
    public ControlGroupFieldTester(ComponentTester tester) {
        this.testId = tester.getTestId();
        if (tester.getPage() != null)
            resolve (tester.getPage());
    }

    @Override
    public void resolve() {
        INode cptEl = page.selectById (this.testId);
        Assertions.assertNotNull (cptEl, "Unable to find node [test-id=\"" + page.resolveTestId(this.testId) + "\"]");

        this.cellEl = cptEl.parent ().parent ();
        Assertions.assertEquals ("cell", this.cellEl.attribute("test-ref"));

        this.rowEl = this.cellEl.parent ();
        Assertions.assertEquals ("row", this.rowEl.attribute("test-ref"));

        this.colEl = this.rowEl.parent ().parent ();
        Assertions.assertEquals ("col", this.colEl.attribute("test-ref"));

        cellEl.matchChild(c->c.nodeName().equalsIgnoreCase("label")).ifPresent(node->labelEl = node);
        Assertions.assertNotNull (this.labelEl);

        this.errorsEl = colEl.selectByRef ("errors");
        Assertions.assertNotNull (this.errorsEl, "Unable to locate errors");

        super.resolve ();
    }

    /**
     * Validates the passed label is present.
     * 
     * @param label the label.
     * @return this tester instance.
     */
    public ControlGroupFieldTester validateLabel (String label) {
        validate (() -> {
            Assertions.assertEquals(label, this.labelEl.textContent ().trim(), "Failed to validate label for control group field with component [test-id=\"" + page.resolveTestId(testId) + "\"]");
        });
        return this;
    }
    
    /**
     * Validates the passed errors are present.
     * 
     * @param errors the errors to test for.
     * @return this tester instance.
     */
    public ControlGroupFieldTester validateError(String...errors) {
        validate (() -> {
            Set<String> errorsOnField = new HashSet<>();
            for (INode child : this.errorsEl.selectByXPath ("li"))
                errorsOnField.add (child.textContent ());
            for (String error : errors)
                Assertions.assertTrue(errorsOnField.contains (error), "Unable to find error \"" + error + "\" for component [test-id=\"" + page.resolveTestId(testId) + "\"]");
        });
        return this;
    }

    @Override
    public <R extends IResolvable> ControlGroupFieldTester with(R resolver, Consumer<R> with) {
        with.accept (resolver);
        return this;
    }
}
