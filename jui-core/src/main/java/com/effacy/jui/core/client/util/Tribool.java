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

/**
 * Convenience to represents something that can take on three states that
 * extends a boolean value with an indeterminate extension.
 *
 * @author Jeremy Buckley
 */
public enum Tribool {
    /**
     * True value.
     */
    TRUE,

    /**
     * False value.
     */
    FALSE,

    /**
     * Undetermined value.
     */
    UNDETERMINED;

    /**
     * Determines if this matches the passed boolean.
     * 
     * @param v
     *          the boolean test to perform.
     * @return {@code true} if there is a match.
     */
    public boolean is(boolean v) {
        if (v)
            return (this == Tribool.TRUE);
        return (this == Tribool.FALSE);
    }

    /**
     * Convenience to test for truth.
     * 
     * @return {@code true} if is.
     */
    public boolean isTrue() {
        return (this == Tribool.TRUE);
    }

    /**
     * Convenience to test for falsity.
     * 
     * @return {@code true} if is.
     */
    public boolean isFalse() {
        return (this == Tribool.FALSE);
    }

    /**
     * Convenience to test for truth.
     * 
     * @return {@code true} if is.
     */
    public boolean isUndetermined() {
        return (this == Tribool.UNDETERMINED);
    }

    /**
     * Runs the runnable if the value matches {@link Tribool#TRUE}.
     * 
     * @param r
     *          the runnable.
     * @return this tribool.
     */
    public Tribool isTrue(Runnable r) {
        if ((r != null) && (this == Tribool.TRUE))
            r.run ();
        return this;
    }

    /**
     * Runs the runnable if the value matches {@link Tribool#FALSE}.
     * 
     * @param r
     *          the runnable.
     * @return this tribool.
     */
    public Tribool isFalse(Runnable r) {
        if ((r != null) && (this == Tribool.FALSE))
            r.run ();
        return this;
    }

    /**
     * Runs the runnable if the value matches {@link Tribool#UNDETERMINED}.
     * 
     * @param r
     *          the runnable.
     * @return this tribool.
     */
    public Tribool isUndetermined(Runnable r) {
        if ((r != null) && (this == Tribool.UNDETERMINED))
            r.run ();
        return this;
    }

}
