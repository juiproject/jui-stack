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
package com.effacy.jui.test;

import java.util.function.Consumer;

import com.effacy.jui.test.Tester.Scoper;
import com.effacy.jui.test.suite.button.ButtonTester;
import com.effacy.jui.test.suite.control.SelectionControlTester;
import com.effacy.jui.test.suite.control.TextControlTester;

public interface ITester<T extends ITester<T>> {

    /**
     * Is passed a resolvable which will first be resolved then will be processed by
     * the passed <code>with</code> consumer.
     * 
     * @param resolver
     *                 the resolvable to be resolved.
     * @param with
     *                 to apply to the resolvable after being resolved.
     * @return this tester instance.
     */
    public <R extends IResolvable> T with(R resolver, Consumer<R> with);

    /**
     * Convenience to logically group actions.
     * 
     * @param with
     *             to apply the actions (to this).
     * @return this tester instance.
     */
    @SuppressWarnings("unchecked")
    default T flow(Consumer<T> with) {
        with.accept ((T) this);
        return (T)this;
    }

    /**
     * Passively applies scope to test ID's.
     * 
     * @param testId
     *               the scoping test ID.
     * @param with
     *               to build out scoped script.
     * @return this tester instance.
     */
    default public T flow(String testId, Consumer<Scoper> with) {
        return with (new Scoper (testId), with);
    }

    /************************************************************************
     * Standard JUI controls (for convenience).
     * <p>
     * For custom conytols call {@link #with(IResolvable, Consumer)} directly
     * passing the tester instance.
     ************************************************************************/

     /**
      * Convenience to attach a {@link ButtonTester}.
      * 
      * @param testId
     *               the ID to attach to.
      * @param with
     *               to process the tester.
      * @return this tester.
      */
    default public T button(String testId, Consumer<ButtonTester> with) {
        return with (ButtonTester.$ (testId), with);
    }

    /**
     * Convenience to attach a {@link TextControlTester}.
     * 
     * @param testId
     *               the ID to attach to.
     * @param with
     *               to process the tester.
     * @return this tester.
     */
    default public T textControl(String testId, Consumer<TextControlTester> with) {
        return with (TextControlTester.$ (testId), with);
    }

    /**
     * Convenience to attach a {@link SelectionControlTester}.
     * 
     * @param testId
     *               the ID to attach to.
     * @param with
     *               to process the tester.
     * @return this tester.
     */
    default public T selectionControl(String testId, Consumer<SelectionControlTester> with) {
        return with (SelectionControlTester.$ (testId), with);
    }
}
