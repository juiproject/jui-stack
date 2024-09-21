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

import java.util.function.Consumer;

/**
 * A very basic view of a control's value behaviour.
 *
 * @author Jeremy Buckley
 */
public interface IControlValue<V> {

    /**
     * Gets the value of the field.
     * 
     * @return The value of the field.
     */
    public V value();


    /**
     * Determines if the field has been modified since the last value set.
     * 
     * @return {@code true} if it is dirty.
     */
    public boolean dirty();

    /**
     * Convenience to invoke the consumer when the control is dirty.
     * 
     * @param applier
     *                to be invoked when dirty (passing the control value).
     * @return {@code true} if was dirty.
     */
    default boolean ifDirty(Consumer<V> applier) {
        if (!dirty())
            return false;
        if (applier != null)
            applier.accept (value ());
        return true;
    }

    /**
     * Builds a control value around raw data.
     * 
     * @param <V>   the value type.
     * @param value
     *              the value to return.
     * @param dirty
     *              the dirty state to return.
     * @return value wrapper.
     */
    public static <V> IControlValue<V> create(V value, boolean dirty) {
        return new IControlValue<V>() {

            @Override
            public V value() {
                return value;
            }

            @Override
            public boolean dirty() {
                return dirty;
            }

        };
    }

}
