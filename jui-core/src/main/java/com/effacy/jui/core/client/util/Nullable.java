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
package com.effacy.jui.core.client.util;

import java.util.Optional;

/**
 * Similar to {@link Optional} but allows for {@code null} values when set.
 *
 * @author Jeremy Buckley
 */
public class Nullable<D> {

    /**
     * See {@link #get()}.
     */
    private D value;

    /**
     * See {@link #isSet()}.
     */
    private boolean set;

    /**
     * Construct without value (not set).
     */
    Nullable() {
        this.set = false;
    }

    /**
     * Construct with a value (can be {@code null}).
     * 
     * @param value
     *              the value.
     */
    Nullable(D value) {
        this.set = true;
        this.value = value;
    }

    /**
     * Gets the value (which could be {@code null}). Only well defined if
     * {@link #isSet()} returns {@code true}).
     * 
     * @return the value (when set).
     */
    public D get() {
        return value;
    }

    /**
     * Determines if a value has been set.
     * 
     * @return {@code true} if it has.
     */
    public boolean isSet() {
        return set;
    }

    /**
     * Construct with a set value.
     * 
     * @param <D>
     *              the value type.
     * @param value
     *              the value.
     * @return a set {@link Nullable} instance.
     */
    public static <D> Nullable<D> of(D value) {
        return new Nullable<D> (value);
    }

    /**
     * Construct with an un-set value.
     * 
     * @param <D>
     *            the value type.
     * @return an unset {@link Nullable} instance.
     */
    public static <D> Nullable<D> unset() {
        return new Nullable<D> ();
    }
}
