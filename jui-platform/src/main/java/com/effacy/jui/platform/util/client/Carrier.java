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
package com.effacy.jui.platform.util.client;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Value carrier.
 * <p>
 * Carries a single value enabling changes to the value. Useful for use with
 * inner classes and lambda expressions.
 */
public class Carrier<V> {

    /**
     * The internal value.
     */
    private V value;
    
    /**
     * Construct with an initial value.
     * @param initial the value.
     */
    protected Carrier(V initial) {
        this.value = initial;
    }

    /**
     * Determines if the value is {@code null}.
     * 
     * @return {@code true} if it is.
     */
    public boolean isNull() {
        return (this.value == null);
    }

    /**
     * Gets the current value.
     * 
     * @return the value.
     */
    public V get() {
        return value;
    }

    /**
     * Gets the current value and modifies the carried value.
     * 
     * @param modifier
     *                 (optional) to modify the current value to a new value (the
     *                 current value is returned, not the modified one).
     * @return the value.
     */
    public V get(Function<V,V> modifier) {
        V current = value;
        if (modifier != null)
            value = modifier.apply (value);
        return current;
    }

    /**
     * Sets the current value.
     * 
     * @param value
     *              the value to set.
     * @return the passed value.
     */
    public V set(V value) {
        this.value = value;
        return value;
    }

    /**
     * Sets the current value if the passed value is different from that held
     * currently.
     * 
     * @param value
     *                   the value to set.
     * @param comparator
     *                   to compare the current value with the passed value (returns
     *                   {@code true} if they are the same).
     * @return {@code true} if there was an update.
     */
    public boolean set(V value, BiFunction<V,V,Boolean> comparator) {
        boolean same = comparator.apply(this.value, value);
        if (!same)
            this.value = value;
        return !same;
    }

    /**
     * Convenience to create with an initial value.
     * 
     * @param <V>     the value type.
     * @param initial
     *                the initial value.
     * @return the carrier appropriately initialised.
     */
    public static <V> Carrier<V> of(V initial) {
        return new Carrier<V> (initial);
    }

    /**
     * Convenience to create with an initial value of {@code null}.
     * 
     * @param <V>     the value type.
     * @return the carrier appropriately initialised.
     */
    public static <V> Carrier<V> of() {
        return new Carrier<V> (null);
    }
}
