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

import com.effacy.jui.test.component.ComponentTester;

public abstract class ControlTester<T extends ControlTester<T>> extends ComponentTester<T> {

    protected ControlTester(String testId, String testCpt) {
        super(testId, testCpt);
    }
    
    /**
     * Validates that the state is {@code invalid}.
     * 
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    public T validateInvalid () {
        validateStateHas ("invalid");
        return (T) this;
    }
    
    /**
     * Validates that the state is not {@code invalid}.
     * 
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    public T validateNotInvalid () {
        validateStateHasNot ("invalid");
        return (T) this;
    }
    
    /**
     * Validates that the state is {@code read-only}.
     * 
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    public T validateReadOnly () {
        validateStateHas ("read-only");
        return (T) this;
    }
    
    /**
     * Validates that the state is not {@code read-only}.
     * 
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    public T validateNotReadOnly () {
        validateStateHasNot ("read-only");
        return (T) this;
    }

    /**
     * Checks if the {@code test-state} contains the passed value.
     * 
     * @param value the value to test for.
     */
    protected void validateStateHas(String value) {
        validate (() -> {
            String state = el.attribute ("test-state");
            if (state == null)
                state = "";
            Assertions.assertTrue (state.contains(value), "State expected to contain \"" + value + "\" but was \"" + state + "\"");
        });
    }

    /**
     * Checks if the {@code test-state} does not contain the passed value.
     * 
     * @param value the value to test for.
     */
    protected void validateStateHasNot(String value) {
        validate (() -> {
            String state = el.attribute ("test-state");
            if (state == null)
                state = "";
            Assertions.assertFalse (state.contains(value), "State expected to NOT contain \"" + value + "\" but was \"" + state + "\"");
        });
    }
}
