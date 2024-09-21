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
package com.effacy.jui.json.client;

import java.util.Date;

import org.gwtproject.json.client.JSONBoolean;
import org.gwtproject.json.client.JSONNull;
import org.gwtproject.json.client.JSONNumber;
import org.gwtproject.json.client.JSONString;
import org.gwtproject.json.client.JSONValue;
import org.gwtproject.regexp.shared.RegExp;

/**
 * Utility methods to support serialization.
 * 
 * @author Jeremy Buckley
 */
public final class SerializerHelper {

    /**
     * Anonymous class matcher.
     */
    private static final RegExp ANONYMOUS_MATCHER = RegExp.compile ("\\$[0-9]+$");


    /**
     * Determines if the passed class is anonymous.
     * 
     * @param klass
     *            the class to check.
     * @return {@code true} if it is.
     */
    public static boolean isAnonymous(Class<?> klass) {
        return ANONYMOUS_MATCHER.test (klass.getName ());
    }


    /**
     * Converts a string to either a {@link JSONNull} (if it is {@code null}) or
     * a {@link JSONString}.
     * 
     * @param string
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    public static JSONValue getString(String string) {
        if (string == null)
            return JSONNull.getInstance ();
        return new JSONString (string);
    }


    /**
     * Converts a boolean to either a {@link JSONNull} (if it is {@code null})
     * or a {@link JSONBoolean}.
     * 
     * @param boolValue
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    public static JSONValue getBoolean(Boolean boolValue) {
        if (boolValue == null)
            return JSONNull.getInstance ();
        return JSONBoolean.getInstance (boolValue);
    }


    /**
     * Converts a boolean to either a {@link JSONNull} (if it is {@code null})
     * or a {@link JSONNumber}.
     * 
     * @param number
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    public static JSONValue getNumber(Number number) {
        if (number == null)
            return JSONNull.getInstance ();
        return new JSONNumber (number.doubleValue ());
    }


    /**
     * Converts a character to either a {@link JSONNull} (if it is {@code null})
     * or a {@link JSONString}.
     * 
     * @param number
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    public static JSONValue getChar(Character character) {
        if (character == null)
            return JSONNull.getInstance ();
        return new JSONString (new String (new char[] {
            character
        }));
    }


    /**
     * Converts a date to either a {@link JSONNull} (if it is {@code null}) or a
     * {@link JSONNumber} wrapping the dates time stamp value (as returned by
     * {@link Date#getTime()}).
     * 
     * @param number
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    public static JSONValue getDate(Date date) {
        if (date == null)
            return JSONNull.getInstance ();
        return new JSONNumber (date.getTime ());
    }


    /**
     * Converts a enum to either a {@link JSONNull} (if it is {@code null}) or a
     * {@link JSONString}.
     * 
     * @param en
     *            the value to convert.
     * @return A JSON typed representation of the value.
     */
    @SuppressWarnings("rawtypes")
    public static JSONValue getEnum(Enum en) {
        if (en == null)
            return JSONNull.getInstance ();
        return new JSONString (en.toString ());
    }


    /**
     * Private constructor.
     */
    private SerializerHelper() {
        // Nothing.
    }
}
