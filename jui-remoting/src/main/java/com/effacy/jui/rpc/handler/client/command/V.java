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
package com.effacy.jui.rpc.handler.client.command;

import java.util.Date;
import java.util.List;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * Wrapper around a value type for the purpose of setting and getting values.
 * 
 * @author Jeremy Buckley
 */
@JsonSerializable
public abstract class V<T> {

    /**
     * The underlying value.
     */
    private T value;

    /**
     * Flag indicating if the value has been set.
     */
    private boolean set = false;


    /**
     * Performs a value assignment. This sets the value and marks the container
     * value as having been set. This should be used for all value assignments.
     * 
     * @param value
     *            the value to assign.
     */
    public void assign(T value) {
        setValue (value);
        setSet (true);
    }

    /**
     * Obtains the value if set (otherwise pass the no set value).
     * 
     * @param noSetValue
     *                   the no set value to return if not set.
     * @return the value or the no set value (when the value is not set).
     */
    public T value(T noSetValue) {
        if (!isSet())
            return noSetValue;
        return getValue ();
    }

    /**
     * Delegates through to {@link #getValue()} but is consistent with the
     * non-setter getter naming convention.
     * 
     * @return the value.
     */
    public T value() {
        return getValue();
    }


    /**
     * Getter for the value.
     * 
     * @return the value.
     */
    public T getValue() {
        return value;
    }


    /**
     * Setter for the value.
     * 
     * @param value
     *            the value.
     */
    public void setValue(T value) {
        this.value = value;
        this.set = true;
    }


    /**
     * Getter for the set status.
     * 
     * @return {@code true} if a value has been assigned.
     */
    public boolean isSet() {
        return set;
    }


    /**
     * Determines if the value is both set and is assigned is a {@code null} value.
     * 
     * @return {@code true} if the value is {@code null}.
     */
    public boolean isNull() {
        return isSet() && (this.value == null);
    }

    /**
     * Determines if the value is set and is not {@code null}.
     * 
     * @return {@code true} if it is.
     */
    public boolean isSetAndNotNull() {
        return isSet() && !isNull();
    }


    /**
     * Setters for the set status.
     * 
     * @param set
     *            {@code true} to make the value as having been set.
     */
    public void setSet(boolean set) {
        this.set = set;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (value == null)
            return "null:" + isSet ();
        return value.toString () + ":" + isSet ();
    }


    /**
     * Convenience to combine set and non-{@code null} check.
     * 
     * @param type
     *            the type to check.
     * @return {@code true} if set and not {@code null}.
     */
    public static boolean setAndNotNull(V<?> type) {
        return (type != null) && type.isSet () && !type.isNull ();
    }


    /**
     * Convenience to combine set and {@code null} check.
     * 
     * @param type
     *            the type to check.
     * @return {@code true} if set and is {@code null}.
     */
    public static boolean setAndNull(V<?> type) {
        return (type != null) && type.isSet () && type.isNull ();
    }


    /**
     * Convenience to test for being set.
     * 
     * @param type
     *            the type to check.
     * @return {@code true} if set.
     */
    public static boolean set(V<?> type) {
        return (type != null) && type.isSet ();
    }

    /**
     * Concrete value type for a Boolean.
     */
    public static class VBoolean extends V<Boolean> {}

    /**
     * Concrete value type for a Long.
     */
    public static class VLong extends V<Long> {}

    /**
     * Concrete value type for a Integer.
     */
    public static class VInteger extends V<Integer> {}

    /**
     * Concrete value type for a Double.
     */
    public static class VDouble extends V<Double> {}

    /**
     * Concrete value type for a String.
     */
    public static class VString extends V<String> {}

    /**
     * Concrete value type for a String.
     */
    public static class VDate extends V<Date> {}

    /**
     * Concrete value type for a list of Long's.
     */
    public static class VListLong extends V<List<Long>> {}

    /**
     * Concrete value type for a list of String's.
     */
    public static class VListString extends V<List<String>> {}
}
