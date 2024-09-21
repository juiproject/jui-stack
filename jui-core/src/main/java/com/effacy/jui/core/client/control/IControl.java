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
package com.effacy.jui.core.client.control;

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.validation.model.IInvalidator;
import com.effacy.jui.validation.model.IValidatable;

public interface IControl<V> extends IComponent, IControlValue<V>, IResetable, IValidatable {

    /**
     * A value assignment that captures the value to assign as well as the behaviour
     * of the control when a value is set (where that behaviour deviates from the
     * default).
     */
    public static class Value<V> {
        /**
         * See {@link #value()}.
         */
        private V value;

        /**
         * See {@link #quiet(boolean)}.
         */
        private boolean quiet;

        /**
         * See {@link #dirty(boolean)}.
         */
        private boolean dirty;

        /**
         * See {@link #force(boolean)}.
         */
        private boolean force;

        /**
         * See {@link #merge(boolean)}.
         */
        private boolean merge;

        /**
         * Construct with the given value to assign.
         * 
         * @param <V>
         *              the value type.
         * @param value
         *              the value being assigned (can be {@code null} if the control
         *              allows for that).
         * @return the value assignment.
         */
        public static <V> Value<V> of(V value) {
            return new Value<V> (value);
        }

        /**
         * Constructs an instance of the value assignment
         * 
         * @param value
         */
        public Value(V value) {
            this.value = value;
        }

        /**
         * The value being assigned (see constructor).
         * 
         * @return the value.
         */
        public V value() {
            return value;
        }

        /**
         * Convenience to call {@link #quiet(boolean)} with {@code true}.
         */
        public Value<V> quiet() {
            return quiet (true);
        }

        /**
         * When the value is assigned, if the value is different from that current on
         * the control, then the control will fire a change event. This can suppress
         * that event.
         * 
         * @param quiet
         *              {@code true} if to quietly update the control (with no events).
         * @return this value instance.
         */
        public Value<V> quiet(boolean quiet) {
            this.quiet = quiet;
            return this;
        }

        /**
         * Getter for {@link #quiet(boolean)}.
         */
        public boolean isQuiet() {
            return quiet;
        }

        /**
         * Convenience to call {@link #dirty(boolean)} with {@code true}.
         */
        public Value<V> dirty() {
            return dirty (true);
        }

        /**
         * When the value is assigned it will not update the reset state (to this value)
         * but (if the value is different from what is currently set) mark the control
         * as dirty (normally this only happens on user input).
         * 
         * @param dirty
         *              {@code true} if to make dirty.
         * @return this value instance.
         */
        public Value<V> dirty(boolean dirty) {
            this.dirty = dirty;
            return this;
        }

        /**
         * Getter for {@link #dirty(boolean)}.
         */
        public boolean isDirty() {
            return dirty;
        }

        /**
         * Convenience to call {@link #force(boolean)} with {@code true}.
         */
        public Value<V> force() {
            return force (true);
        }

        /**
         * When the value is assigned a modified event will always be fired (even if the
         * value is the same as what is current).
         * 
         * @param force
         *              {@code true} if to force.
         * @return this value instance.
         */
        public Value<V> force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * Getter for {@link #force(boolean)}.
         */
        public boolean isForce() {
            return force;
        }

        /**
         * Convenience to call {@link #merge(boolean)} with {@code true}.
         */
        public Value<V> merge() {
            return merge(true);
        }

        /**
         * Whether to merge the value into the existing value (default is not to).
         * <p>
         * This is very much dependent on the control value and implementation. There is
         * no universal value-neutral definition of what merge means so is something
         * that generally is not employed.
         * 
         * @param merge
         *              {@code true} if to merge.
         * @return this value instance.
         */
        public Value<V> merge(boolean merge) {
            this.merge = merge;
            return this;
        }

        /**
         * Getter for {@link #merge(boolean)}.
         */
        public boolean isMerge() {
            return merge;
        }
    }

    /**
     * The fields name. This will be used to reference the field (but is not
     * displayed).
     * 
     * @return The name of the field.
     */
    public String getName();

    /**
     * Determines if the field has no value.
     * 
     * @return {@code true} if the control has no value.
     */
    public boolean isEmpty();

    /**
     * Sets the value on the field.
     * <p>
     * If the value has changed then a change event will be fired.
     * 
     * @param value
     *              the value of the field.
     */
    default public void setValue(V value) {
        setValue (Value.of (value));
    }

    /**
     * Sets the value on the field.
     * 
     * @param value
     *              the value (with behaviour) of the field.
     */
    public void setValue(Value<V> value);

    /**
     * Mark the control as being read-only.
     * 
     * @param readOnly
     *                 {@code true} if read-only.
     */
    public void readOnly(boolean readOnly);

    /**
     * Determines if the field is read-only.
     * 
     * @return {@code true} if it is.
     * @see {@link #readOnly(boolean)}.
     */
    public boolean isReadOnly();

    /**
     * Suspends the control. This will maintain its current visual state but will
     * not partake in validations.
     * 
     * @param suspended
     *                  {@code true} to suspend.
     */
    public void suspend(boolean suspended);

    /**
     * Determines if the control has been suspended (see {@link #suspend(boolean)}).
     * 
     * @return {@code true} if it has.
     */
    public boolean isSuspended();

    /**
     * Marks the control as waiting for data to be provided (i.e. loading).
     * 
     * @param waiting
     *                {@code true} if to mark in the waiting state.
     */
    public void waiting(boolean waiting);

    /**
     * Obtains the invalidator for the control.
     * 
     * @return the invalidator.
     */
    public IInvalidator invalidator();

    /**
     * {@inheritDoc}
     * <p>
     * Default is to delegate to {@link #invalidator()}.
     *
     * @see com.effacy.jui.validation.model.IValidatable#valid()
     */
    default public boolean valid() {
        return invalidator ().valid ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default is to delegate to {@link #invalidator()}.
     *
     * @see com.effacy.jui.validation.model.IValidatable#validate()
     */
    default public boolean validate() {
        return invalidator ().validate ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default is to delegate to {@link #invalidator()}.
     *
     * @see com.effacy.jui.validation.model.IValidatable#validate(boolean)
     */
    default public boolean validate(boolean clearIfValid) {
        return invalidator ().validate (clearIfValid);
    }

}
