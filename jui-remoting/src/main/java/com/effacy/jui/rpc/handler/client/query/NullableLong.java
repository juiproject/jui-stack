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
package com.effacy.jui.rpc.handler.client.query;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * A long value that can also be {@code null} during serialisable and use within
 * javascript.
 *
 * @author Jeremy Buckley
 */
@JsonSerializable
public class NullableLong {

    /**
     * The value.
     */
    private long value;

    /**
     * If the value has been set.
     */
    private boolean set;


    /**
     * Default (empty) construct. Value is considered not set (aka {@code null}
     * ).
     */
    public NullableLong() {
        clear ();
    }


    /**
     * Construct with a value.
     * 
     * @param value
     *            the value.
     */
    public NullableLong(long value) {
        assign (value);
    }


    /**
     * The value of the nullable long as a box type.
     * 
     * @return the value.
     */
    public Long value() {
        if (isNull ())
            return null;
        return getValue ();
    }


    /**
     * Gets the value passing the default through if {@code null}.
     * 
     * @param defaultValue
     *            the default value when {@code null}.
     * @return the value.
     */
    public long value(long defaultValue) {
        if (isNull ())
            return defaultValue;
        return getValue ();
    }


    /**
     * Assign by way of copy. If the copy is {@code null} then it will be
     * treated as a call to {@link #clear()}).
     * 
     * @param copy
     *            the copy to make.
     */
    public void assign(NullableLong copy) {
        if ((copy == null) || copy.isNull ())
            clear ();
        else
            assign (copy.getValue ());
    }


    /**
     * Assign a value.
     * 
     * @param value
     *            the value to assign.
     */
    public void assign(Long value) {
        if (value == null) {
            clear ();
        } else {
            this.set = true;
            this.value = value;
        }
    }


    /**
     * Clear the value (nullify it).
     */
    public void clear() {
        this.value = 0;
        this.set = false;
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
     * Determines if this should be treated as a null value (this is just the
     * logical negation of {@link #isSet()}).
     * 
     * @return {@code true} if it should.
     */
    public boolean isNull() {
        return !isSet ();
    }


    /**
     * Gets the value (only defined if {@link #isSet()} return {@code true}).
     * 
     * @return the value.
     */
    public long getValue() {
        return value;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (isSet ())
            return Long.toString (value);
        return "null";
    }


    /***********************************************************************************
     * Setters for serialisation.
     */

    public void setValue(long value) {
        this.value = value;
    }


    public void setSet(boolean set) {
        this.set = set;
    }

}
