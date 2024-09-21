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

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

/**
 * Supporting utilities for working with strings.
 *
 * @author Jeremy Buckley
 */
public final class StringSupport {

    /**
     * The character used to split on (by default).
     */
    public static final String SPLIT_CHAR = "\r";

    /**
     * Compose a collection of lines into a single line that can be passed to the
     * the dom builder as splittable text.
     * 
     * @param lines the lines to compose.
     * @return the composed line.
     */
    public static String compose(String...lines) {
        String str = null;
        for (String line : lines) {
            if (str == null)
                str = "";
            else
                str += SPLIT_CHAR;
            if (line == null)
                continue;
            str += line;
        }
        return (str == null) ? "" : str;
    }

    /**
     * Splits a string to invert {@link #compose(String...)}.
     * 
     * @param str
     *            the string to split (a {@code null} string will split to an empty
     *            array).
     * @return the split string.
     */
    public static String [] split(String str) {
        if (str == null)
            return new String [0];
        return str.split (SPLIT_CHAR);
    }

    /**
     * Performs a trim and truncates to a maximum length adding ellipses if
     * truncating.
     * 
     * @param str
     *            the string to trim.
     * @param maxLength
     *            the maximum length.
     * @return the trimmed and truncated string.
     */
    public static String trim(String str, int maxLength) {
        str = trim (str);
        maxLength = Math.max (0, maxLength - 3);
        if (str.length () > maxLength)
            str = str.substring (0, maxLength) + "...";
        return str;
    }


    /**
     * Determines if the passed string, after trimming, is empty.
     * 
     * @param str
     *            the string to test.
     * @return {@code true} of the string is {@code null} or is empty when
     *         trimmed.
     */
    public static boolean empty(String str) {
        if (str == null)
            return true;
        return str.trim ().isEmpty ();
    }


    /**
     * Determines if the passed string, after trimming, is empty. If it is it
     * returns the alternative string otherwise the passed one.
     * 
     * @param str
     *            the string to test.
     * @param alt
     *            the alternative string.
     * @return if empty then the alternative string, otherwise the passed one.
     */
    public static String empty(String str, String alt) {
        if (empty (str))
            return alt;
        return str;
    }


    /**
     * Given a string performs an HTML escaping of the string for safe injection
     * into HTML.
     * 
     * @param str
     *            the string to escape.
     * @return the escaped string (empty string if the passed string was
     *         {@code null}).
     */
    public static String escape(String str) {
        if (str == null)
            return "";
        return new SafeHtmlBuilder ().appendEscaped (str).toSafeHtml ().asString ();
    }

    /**
     * Makes the first letter of the string lower case.
     * 
     * @param str
     *            the string to convert.
     * @return The converted string.
     */
    public static String lowerCaseFirstLetter(String str) {
        str = trim (str);
        if (str.isEmpty ())
            return "";
        return str.substring (0, 1).toLowerCase () + str.substring (1);
    }

    /**
     * Makes the first letter of the string upper case.
     * 
     * @param str
     *            the string to convert.
     * @return The converted string.
     */
    public static String upperCaseFirstLetter(String str) {
        str = trim (str);
        if (str.isEmpty ())
            return "";
        return str.substring (0, 1).toUpperCase () + str.substring (1);
    }

    public static String markupQuotes(String str, String markupLeft, String markupRight, boolean insideQuote) {
        str = trim (str);
        if (str.isEmpty ())
            return "";

        StringBuffer converted = new StringBuffer ();
        boolean left = true;
        while (true) {
            int i = str.indexOf ('"');
            if (i < 0) {
                converted.append (str);
                return converted.toString ();
            }
            converted.append (str.subSequence (0, i));
            str = str.substring (i + 1);
            if (insideQuote) {
                if (left) {
                    converted.append ('"');
                    converted.append (markupLeft);
                } else {
                    converted.append (markupRight);
                    converted.append ('"');
                }
            } else {
                if (left) {
                    converted.append (markupLeft);
                    converted.append ('"');
                } else {
                    converted.append ('"');
                    converted.append (markupRight);
                }
            }
            left = !left;
        }
    }


    /**
     * Trims the passed string returning the empty string when the passed string
     * is {@code null}.
     * 
     * @see #trim(String, String).
     * @param str
     *            the string to trim.
     * @return the trimmed string.
     */
    public static String trim(String str) {
        return trim (str, "");
    }


    /**
     * Removes leading and trailing whitespace from a copy of the passed string.
     * If the string is {@code null} then the passed null value is returned.
     * 
     * @see {String{@link #trim(String)}.
     * @param str
     *            the string to trim.
     * @param nullValue
     *            the value to return if the passed string is {@code null}.
     * @return the trimmed string.
     */
    public static String trim(String str, String nullValue) {
        return (str == null) ? nullValue : str.trim ();
    }


    /**
     * Truncates a string to the provided then and adds ellipses.
     * 
     * @param str
     *            the string to truncate.
     * @param length
     *            the length to truncate to.
     * @return the truncated string.
     */
    public static String truncate(String str, int length) {
        str = trim (str);
        if (str.length () > length) {
            str = str.substring (0, length);
            str += '\u2026';
        }
        return str;
    }


    /**
     * Returns an empty string if the passed string is {@code null}, otherwise
     * passes through the string.
     * <p>
     * No trimming is performed.
     * 
     * @param str
     *            the string to make {@code null}-safe.
     * @return the string (or empty string).
     */
    public static String safe(String str) {
        return (str == null) ? "" : str;
    }


    /**
     * Determines if the passed strings are the same (upto blankness and trimming).
     * <p>
     * Not that {@code null} is treated as the empty string (under blankness).
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == str2)
            return true;
        if (str1 == null)
            return str2.trim ().isEmpty ();
        if (str2 == null)
            return str1.trim ().isEmpty ();
        return str1.trim ().equals (str2.trim ());
    }

    /**
     * Used by {@link #loremIpsum(int)}.
     */
    private static String [] LOREM_IPSUM = new String [] {
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    };

    /**
     * Convenience to generate placeholder text during concept development.
     * 
     * @param sentences
     *                  the number of sentences to include.
     * @return a paragraph of lorem ipsum text consisting of the given number of
     *         sentences.
     */
    public static String loremIpsum(int sentences) {
        if (sentences <= 0)
            return "";
        if (sentences == 1)
            return LOREM_IPSUM[0];
        String str = LOREM_IPSUM[0];
        for (int i = 1; i < sentences; i++)
            str += " " + LOREM_IPSUM[i % LOREM_IPSUM.length];
        return str;
    }


    /**
     * Private constructor.
     */
    private StringSupport() {
        // Nothing.
    }
}
