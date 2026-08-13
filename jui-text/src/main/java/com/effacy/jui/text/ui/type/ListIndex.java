/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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
package com.effacy.jui.text.ui.type;

/**
 * Ordered-list marker formatting, shared by the editor and the read-only
 * renderer.
 * <p>
 * It lives here, in one place, because the two surfaces are supposed to present
 * formatted text identically. A numbering scheme duplicated in each is a scheme
 * that will eventually disagree with itself — and a list that numbers one way
 * while being edited and another way when read is worse than either.
 */
public final class ListIndex {

    private ListIndex() {}

    /**
     * Formats a list counter for an indent level: numeric at level 0, lowercase
     * alpha at level 1, lowercase roman at level 2, then cycling.
     *
     * @param indent
     *                the indent level (0 upwards).
     * @param counter
     *                the sequential counter value (1-based).
     * @return the display string (e.g. {@code "1"}, {@code "a"}, {@code "iii"}).
     */
    public static String format(int indent, int counter) {
        switch (Math.abs (indent) % 3) {
            case 1:
                return toLetter (counter);
            case 2:
                return toRoman (counter);
            default:
                return String.valueOf (counter);
        }
    }

    private static String toLetter(int n) {
        StringBuilder sb = new StringBuilder ();
        while (n > 0) {
            n--;
            sb.insert (0, (char) ('a' + (n % 26)));
            n /= 26;
        }
        return sb.toString ();
    }

    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    private static final String[] ROMAN_SYMBOLS = {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};

    private static String toRoman(int n) {
        if (n <= 0)
            return String.valueOf (n);
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (n >= ROMAN_VALUES[i]) {
                sb.append (ROMAN_SYMBOLS[i]);
                n -= ROMAN_VALUES[i];
            }
        }
        return sb.toString ();
    }

}
