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

public final class Random {

    /**
     * See the JavaScript Math.random() function.
     */
    public static native double nextDouble() /*-{
        return Math.random();
    }-*/;

    /**
     * Returns a random <code>int</code> between -2147483648 and 2147483647
     * (inclusive) with roughly equal probability of returning any particular
     * <code>int</code> in this range. The underlying browser's random
     * implementation is used.
     */
    public static native int nextInt() /*-{
        // "|0" forces the value to a 32 bit integer.
        return (Math.floor(Math.random() * 4294967296) - 2147483648) | 0;
    }-*/;

    /**
     * Returns a random <code>int</code> between 0 (inclusive) and
     * <code>upperBound</code> (exclusive) with roughly equal probability of
     * returning any particular <code>int</code> in this range. The underlying
     * browser's random implementation is used.
     */
    public static native int nextInt(int upperBound) /*-{
        // "|0" forces the value to a 32 bit integer.
        return (Math.floor(Math.random() * upperBound)) | 0;
    }-*/;

    /**
     * Not instantiable. Having different instances of this class would not be
     * meaningful because no state is stored and the common browser implementation
     * is shared.
     */
    private Random() {
    }
}
