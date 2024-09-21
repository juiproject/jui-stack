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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Expresses a condition evaluated against some data.
 */
public interface Condition<D> {

    /**
     * Given the current data express a condition against that data.
     * 
     * @param data
     *            the data to evaluate against.
     * @return {@code true} if to continue.
     */
    public boolean test(D data);


    /**
     * Creates a not version of this condition.
     * 
     * @return the not version.
     */
    default public Condition<D> not() {
        return new NotCondition<D> (this);
    }


    /**
     * Creates an AND condition with this condition and the passed condition.
     */
    default public Condition<D> and(Condition<D> cond) {
        return new AndCondition<D> (this, cond);
    }


    /**
     * Creates an OR condition with this condition and the passed condition.
     */
    default public Condition<D> or(Condition<D> cond) {
        return new OrCondition<D> (this, cond);
    }

    /************************************************************************
     * Standard implementations.
     ************************************************************************/

    /**
     * Negation of a {@link Condition}.
     */
    public static class NotCondition<D> implements Condition<D> {

        /**
         * The condition to negate.
         */
        private Condition<D> condition;

        /**
         * Construct with a condition.
         * 
         * @param condition
         *            the condition to negate.
         */
        public NotCondition(Condition<D> condition) {
            this.condition = condition;
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (condition == null)
                return false;
            return !condition.test (data);
        }

    }

    /**
     * An AND condition.
     */
    public static class AndCondition<D> implements Condition<D> {

        /**
         * The condition to negate.
         */
        private List<Condition<D>> conditions = new ArrayList<Condition<D>> ();

        /**
         * Construct with a condition.
         * 
         * @param condition
         *            the condition to negate.
         */
        @SafeVarargs
        public AndCondition(Condition<D>... conditions) {
            for (Condition<D> condition : conditions)
                this.conditions.add (condition);
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            for (Condition<D> condition : conditions) {
                if (!condition.test (data))
                    return false;
            }
            return true;
        }

    }

    /**
     * An OR condition.
     */
    public static class OrCondition<D> implements Condition<D> {

        /**
         * The condition to negate.
         */
        private List<Condition<D>> conditions = new ArrayList<Condition<D>> ();

        /**
         * Construct with a condition.
         * 
         * @param condition
         *            the condition to negate.
         */
        @SafeVarargs
        public OrCondition(Condition<D>... conditions) {
            for (Condition<D> condition : conditions)
                this.conditions.add (condition);
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            for (Condition<D> condition : conditions) {
                if (condition.test (data))
                    return true;
            }
            return false;
        }

    }

    /**
     * Conditional check for a non-emptiness.
     */
    public static class NotEmptyCondition<D> implements Condition<D> {

        /**
         * The provider to check.
         */
        private Provider<?, D> provider;

        /**
         * Construct with a condition.
         * 
         * @param condition
         *            the condition to negate.
         */
        public NotEmptyCondition(Provider<?, D> provider) {
            this.provider = provider;
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (provider == null)
                return false;
            Object value = provider.get (data);
            if (value == null)
                return false;
            if (value instanceof String)
                return !StringSupport.empty ((String) value);
            if (value instanceof Collection)
                return !((Collection<?>) value).isEmpty ();
            return true;
        }

    }

    /**
     * A condition that always returns {@code true}.
     */
    public static class TrueCondition<D> implements Condition<D> {

        @Override
        public boolean test(D data) {
            return true;
        }

    }

    /**
     * A condition that always returns {@code false}.
     */
    public static class FalseCondition<D> implements Condition<D> {

        @Override
        public boolean test(D data) {
            return false;
        }

    }
}
