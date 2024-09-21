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
package com.effacy.jui.core.client.dom.renderer.template;

import com.effacy.jui.core.client.dom.renderer.template.Condition.FalseCondition;
import com.effacy.jui.core.client.dom.renderer.template.Condition.NotEmptyCondition;
import com.effacy.jui.core.client.dom.renderer.template.Condition.TrueCondition;

/**
 * Useful tool for builder conditions. See {@link ConditionSupport} for a
 * version that can be applied to classes.
 *
 * @author Jeremy Buckley
 */
public final class ConditionBuilder {

    /**
     * Convenience to provide an explicit cast.
     * <p>
     * See {@link ConditionSupport#createCondition(Condition)}.
     */
    public static <D> Condition<D> createCondition(Condition<D> provider) {
        if (provider == null)
            return falseCondition ();
        return provider;
    }

    /**
     * A condition that is always true.
     * <p>
     * See {@link ConditionSupport#trueCondition()}.
     */
    public static <D> Condition<D> trueCondition() {
        return new TrueCondition<D> ();
    }

    /**
     * A condition that is always true.
     * <p>
     * See {@link ConditionSupport#falseCondition()}.
     */
    public static <D> Condition<D> falseCondition() {
        return new FalseCondition<D> ();
    }

    /**
     * A condition that checks for a non-empty provider.
     * <p>
     * See {@link ConditionSupport#notEmptyCondition(Provider)}.
     */
    public static <D> Condition<D> notEmptyCondition(Provider<?, D> provider) {
        return new NotEmptyCondition<D> (provider);
    }

    /**
     * A condition that checks for an empty provider.
     * <p>
     * See {@link ConditionSupport#emptyCondition(Provider)}.
     */
    public static <D> Condition<D> emptyCondition(Provider<?, D> provider) {
        return new NotEmptyCondition<D> (provider).not ();
    }

    /**
     * A condition that checks for a specific provider value.
     * <p>
     * See {@link ConditionSupport#valueCondition(Provider, Object)}}.
     */
    public static <V, D> Condition<D> valueCondition(Provider<V, D> provider, V value) {
        return new Condition<D> () {

            @Override
            public boolean test(D data) {
                if (!provider.test (data))
                    return false;
                V providedValue = provider.get (data);
                if (providedValue == value)
                    return true;
                if ((providedValue != null) && (value != null))
                    return providedValue.equals (value);
                return false;
            }
        };
    }

    /**
     * A condition that checks for a {@code true} provider.
     */
    public static <D> Condition<D> trueCondition(final Provider<Boolean, D> provider) {
        return new Condition<D> () {

            @Override
            public boolean test(D data) {
                if (provider == null)
                    return false;
                Boolean result = provider.get (data);
                return (result != null) && result.booleanValue ();
            }

        };
    }

    /**
     * A condition that checks for a {@code false} provider.
     */
    public static <D> Condition<D> falseCondition(final Provider<Boolean, D> provider) {
        return new Condition<D> () {

            @Override
            public boolean test(D data) {
                if (provider == null)
                    return true;
                Boolean result = provider.get (data);
                return (result == null) || !result.booleanValue ();
            }

        };
    }

    /**
     * Provide non-construct constructor.
     */
    private ConditionBuilder() {
        // Nothing.
    }
}
