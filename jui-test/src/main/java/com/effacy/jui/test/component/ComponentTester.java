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
package com.effacy.jui.test.component;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IPage;
import com.effacy.jui.test.IResolvable;
import com.effacy.jui.test.Tester;
import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.suite.control.ControlGroupFieldTester;

/**
 * A type of {@link Tester} that is specific to testing components.
 * <p>
 * Passed is a test ID that is used to map to a DOM node that should be the root
 * node of the associated component type. This test ID may be scoped by the
 * {@link IPage} that is passed during resolution.
 */
public class ComponentTester<C extends ComponentTester<C>> extends Tester<C> {

    /**
     * The (local) test ID for the component instance.
     */
    protected String testId;

    /**
     * The test component.
     */
    protected String testCpt;

    /**
     * The root node.
     */
    protected INode el;

    /**
     * Construct instance with a test ID.
     * 
     * @param testId  the (local) test ID.
     * @param testCpt the test Cpt to validate.
     */
    public ComponentTester(String testId, String testCpt) {
        this.testId = testId;
        this.testCpt = testCpt;
    }

    /**
     * Marks this as being a sub-class but without specifying what the type is (and
     * so no sub-class check is performed).
     * 
     * @return this tester instance.
     */
    public C subclass() {
        return subclass (null);
    }

    /**
     * Marks this as being a sub-class of the given type (so will check for that,
     * unless it is {@code null} in which case no check is performed).
     * 
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    public C subclass(String subclassName) {
        testCpt = ((subclassName == null) || subclassName.isBlank ()) ? null : subclassName.toLowerCase ().trim ();
        return (C) this;
    }

    /**
     * The (local) test ID.
     * 
     * @return the test ID.
     */
    public String getTestId() {
        return testId;
    }

    @Override
    public void resolve() {
        Assertions.assertNotNull (this.page, "Unable to resolve as no page is available");

        this.el = page.selectById (testId);
        Assertions.assertNotNull (this.el, "Unable to find node [test-id=\"" + page.resolveTestId (testId) + "\"]");
        if (testCpt != null)
            assertTestCpt (testCpt);

        // Validate those things that were registered.
        super.resolve();
    }

    /**
     * Asserts that the tester root node has a {@code test-cpt} attribute and it is
     * of the passed value.
     * 
     * @param testCpt the expected value of the {@code test-cpt} attribute.
     */
    protected void assertTestCpt(String testCpt) {
        if (testCpt == null)
            return;
        String actualTestCpt = el.attribute("test-cpt");
        Assertions.assertEquals(testCpt, actualTestCpt);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IResolvable> C with(T resolver, Consumer<T> with) {
        if (resolver != null) {
            resolver.resolve (page.wrap (testId));
            if (with != null)
                with.accept (resolver);
        }
        return (C) this;
    }

    /**
     * Assumes the node component resides in a {@link ControlSectionGroup} and
     * locates the associated field data.
     * 
     * @param with to process the field tester.
     * @return this tester instance.
     */
    public C field (Consumer<ControlGroupFieldTester> with) {
        // Note that we do not pass the ID as the scope is already on the target
        // control.
        return with (new ControlGroupFieldTester (""), with);
    }
}
