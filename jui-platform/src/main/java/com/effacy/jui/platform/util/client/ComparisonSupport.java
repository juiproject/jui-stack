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

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * General utilities for performing comparisons.
 * 
 * @author Jeremy Buckley
 */
public final class ComparisonSupport {

    /**
     * Private constructor.
     */
    private ComparisonSupport() {
        // Nothing.
    }

    /**
     * Provides a {@code null}-safe string comparison that can be used in sorting
     * that is consistent with <code>b.compareTo(a)</code> (so preserves the natural
     * ordering of strings where "a" is greater than "b").
     * <p>
     * In this case {@code null} is treated as an empty string.
     * 
     * @param a
     *          the first value.
     * @param b
     *          the second value.
     * @return the comparison result.
     */
    public static int compare(String a, String b) {
        a = StringSupport.trim(a);
        b = StringSupport.trim(b);
        return b.compareTo(a);
    }

    /**
     * Determines if two elements of the same type are equal or not.
     * <p>
     * Note that for strings, arrays and collection empty and {@code null} are synonymous.
     * 
     * @return {@code true} if they are equal.
     */
    public static <T> boolean equal(T a, T b) {
        if (a == b)
            return true;
        boolean aEmpty = empty (a);
        boolean bEmpty = empty (b);
        if (aEmpty)
            return bEmpty;
        if (bEmpty)
            return false;
        if (a.getClass().isArray ()) {
            int la = arrayLength (a);
            int lb = arrayLength (b);
            if (la != lb)
                return false;
            for (int i = 0; i < la; i++) {
                if (!ComparisonSupport.equal (arrayAt (a, i), arrayAt (b, i)))
                    return false;
            }
            return true;
        }
        if (a instanceof Collection) {
            Collection<?> aCol = (Collection<?>) a;
            Collection<?> bCol = (Collection<?>) b;
            if (aCol.size () != bCol.size ())
                return false;
            // This is not the most efficient but we do assume that the
            // collections will generally be quite small so will still be
            // reasonably fast.
            LOOP: for (Object aObj : aCol) {
                for (Object bObj : bCol) {
                    if (equal (aObj, bObj))
                        continue LOOP;
                }
                return false;
            }
            return true;
        }
        return a.equals (b);
    }

    /**
     * Obtains the element of the passed array at the given index.
     * 
     * @param array
     *              the array.
     * @param idx
     *              the index.
     * @return the element.
     */
    protected static Object arrayAt(Object array, int idx) {
        return Array.get (array, idx);
    }

    /**
     * Obtains the length pf the passed array.
     * 
     * @param array
     *              the array
     * @return the length.
     */
    protected static int arrayLength(Object array) {
        return Array.getLength (array);
    }


    /**
     * The logical negative of {@link ComparisonSupport#equal(Object, Object)}.
     * 
     * @return {@code true} if they are not equal.
     */
    public static <T> boolean notEqual(T a, T b) {
        return !equal (a, b);
    }


    /**
     * Returns the passed value or the default value if the former is
     * {@code null}.
     * 
     * @return {@code value} or {@code def} if the value is {@code null}.
     */
    public static <T> T valueOrDefault(T value, T def) {
        return (value != null) ? value : def;
    }


    /**
     * Determines if the passed value is empty (meaning {@code null}, empty
     * collection or empty string).
     * 
     * @param value
     *            the value to test.
     * @return {@code true} if the value is empty.
     */
    @SuppressWarnings("rawtypes")
    public static <T> boolean empty(T value) {
        if (value == null)
            return true;
        if (value instanceof String)
            return ((String) value).trim ().isEmpty ();
        if (value instanceof Collection)
            return ((Collection) value).isEmpty ();
        if (value.getClass().isArray())
            return (arrayLength (value) == 0);
        return false;
    }
}
