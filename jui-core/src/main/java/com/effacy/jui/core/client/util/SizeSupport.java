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

import java.util.function.BiFunction;

/**
 * Utilities for working with various units of measurement.
 * 
 * @author Jeremy Buckley
 */
public class SizeSupport {

    /************************************************************************
     * Data size related utilities.
     ************************************************************************/

    private final static String [] CAPS_SIZES = new String[] {
        "B", "KB", "MB", "GB", "TB", "PB"
    };
    public static BiFunction<Boolean,Integer,String> CAPS = (single,index) -> {
        return CAPS_SIZES[index];
    };

    private final static String [] MIXED_SIZES = new String[] {
        "b", "Kb", "Mb", "Gb", "Tb", "Pb"
    };
    public static BiFunction<Boolean,Integer,String> MIXED = (single,index) -> {
        return MIXED_SIZES[index];
    };

    private final static String [] FULL_SINGLE = new String[] {
        "byte", "Kbyte", "Mbyte", "Gbyte", "Tbyte", "Pbyte"
    };
    private final static String [] FULL_PLURAL = new String[] {
        "bytes", "Kbytes", "Mbytes", "Gbytes", "Tbytes", "Pbytes"
    };
    public static BiFunction<Boolean,Integer,String> FULL = (single,index) -> {
        return single ? FULL_SINGLE[index] : FULL_PLURAL[index];
    };

    /**
     * Given a size in bytes, converts to a human readable form to the nearest
     * sensible size.
     * 
     * @param size
     *            the size (in bytes).
     * @param dp
     *            the number of decimal places to round to.
     * @return The human readable form.
     */
    public static String convertToDataSize(int size, int dp) {
        return convertToDataSize (size, dp, null);
    }

    /**
     * Given a size in bytes, converts to a human readable form to the nearest
     * sensible size.
     * <p>
     * One can provide a suffix mapper. This is a bi-function that takes a boolean
     * (if the value is singular) and an index (for the degree of suffix). If the
     * index is 0 then it should return a suffix suitable for "bytes", for 1 it
     * should be suitable for "kilobytes". The maximum index is 5 (petabytes).
     * <p>
     * The default suffix is "B", "KB", "MB", etc. A mixed case version is given by
     * {@link #MIXED} and a more full version (expanding "b" to "bytes") is given by
     * {@link #FULL}.
     * 
     * @param size
     *               the size (in bytes).
     * @param dp
     *               the number of decimal places to round to.
     * @param suffix
     *               (optional) suffix mapper (see description above).
     * @return The human readable form.
     */
    public static String convertToDataSize(int size, int dp, BiFunction<Boolean,Integer,String> suffix) {
        return _convertToDataSize (size, dp, (suffix == null) ? CAPS : suffix);
    }


    /**
     * See {@link #convertToDataSize(int, int, BiFunction)}. Is delegated to by that
     * method such that the suffix map is never {@code null}.
     */
    private static String _convertToDataSize(int size, int dp, BiFunction<Boolean,Integer,String> suffix) {
        if (size < 0)
            return "0" + suffix.apply (false, 0);

        // Convert to the highest reasonable radix as a double.
        double converted = (double) size;
        int n = 0;
        while ((n < 6) && (converted > 1024.0)) {
            converted /= 1024.0;
            n++;
        }

        // Extract the whole and fractional parts (as determined by the dp).
        int whole = (int) converted;
        converted -= whole;
        while (dp-- > 0)
            converted *= 10;
        int part = (int) converted;

        // Remove any trailing 0's in the part.
        while ((part > 0) && (part % 10 == 0))
            part /= 10;

        // Format the string.
        if (part == 0)
            return Integer.toString (whole) + suffix.apply (whole == 1, n);
        return Integer.toString (whole) + "." + Integer.toString (part) + suffix.apply (false, n);
    }


    /**
     * Private constructor.
     */
    private SizeSupport() {
        // Nothing.
    }
}
