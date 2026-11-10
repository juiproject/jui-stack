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
package com.effacy.jui.platform.css.rebind;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Carries CSS dimensions whose unit the CSS parser (ph-css) does not know
 * through parsing.
 * <p>
 * The parser splits a dimension with a unit it does not know (such as
 * {@code 18lh}) into a number and an identifier (written out as
 * {@code 18 lh}, which a browser drops), and fails on one inside
 * {@code calc(...)} (losing the whole rule). {@link #protect(String, Map)}
 * replaces each such dimension in a declaration value with a placeholder the
 * parser carries in any value ({@code var(--jui-unit-N)}), and
 * {@link #restore(String, Map)} puts the dimension back in the parser's output.
 * <p>
 * Only declaration values are changed: selectors and at-rule preludes (such as
 * media queries) are left as they are, as are strings, comments and
 * {@code url(...)}.
 */
public final class CssUnits {

    /**
     * Valid CSS units that the parser does not know (all lower case; units are
     * case insensitive).
     */
    public static final Set<String> UNITS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        // Font-relative lengths.
        "cap", "ic", "lh", "rlh", "rex", "rch", "rcap", "ric",
        // Viewport-relative lengths (logical, small, large and dynamic).
        "vi", "vb",
        "svw", "svh", "svi", "svb", "svmin", "svmax",
        "lvw", "lvh", "lvi", "lvb", "lvmin", "lvmax",
        "dvw", "dvh", "dvi", "dvb", "dvmin", "dvmax",
        // Container query lengths.
        "cqw", "cqh", "cqi", "cqb", "cqmin", "cqmax",
        // Resolution (as used by image-set()).
        "x"
    )));

    /**
     * Prefix of the custom property named in a placeholder.
     */
    private static final String PLACEHOLDER_PREFIX = "var(--jui-unit-";

    private CssUnits() {
        // Static helpers only.
    }

    /**
     * Replaces each dimension (in a declaration value) whose unit is one of
     * {@link #UNITS} with a placeholder.
     *
     * @param content
     *                     the CSS content.
     * @param placeholders
     *                     the placeholders allocated so far, to which each new
     *                     placeholder is added (mapped to the dimension it
     *                     replaces); placeholders are numbered from the size of
     *                     this map.
     * @return the content with the placeholders.
     */
    public static String protect(String content, Map<String, String> placeholders) {
        if (content == null)
            return null;
        StringBuilder out = new StringBuilder(content.length());
        // The text of the current region (since the last '{', '}' or ';') as it is
        // and with placeholders. Which is used depends on how the region ends.
        StringBuilder raw = new StringBuilder();
        StringBuilder replaced = new StringBuilder();
        int depth = 0;
        int n = content.length();
        int i = 0;
        while (i < n) {
            char c = content.charAt(i);
            int end = -1;
            if ((c == '/') && ((i + 1) < n) && (content.charAt(i + 1) == '*')) {
                // Comment.
                int close = content.indexOf("*/", i + 2);
                end = (close < 0) ? n : (close + 2);
            } else if ((c == '"') || (c == '\'')) {
                // String.
                end = i + 1;
                while (end < n) {
                    char d = content.charAt(end);
                    if (d == '\\') {
                        end += 2;
                        continue;
                    }
                    end++;
                    if (d == c)
                        break;
                }
                end = Math.min(end, n);
            } else if (((c == 'u') || (c == 'U')) && content.regionMatches(true, i, "url(", 0, 4) && ((i == 0) || !identChar(content.charAt(i - 1)))) {
                // Unquoted URL (a quoted one is closed at the first ')' after the string,
                // which is good enough here).
                int close = content.indexOf(')', i + 4);
                end = (close < 0) ? n : (close + 1);
            }
            if (end > 0) {
                raw.append(content, i, end);
                replaced.append(content, i, end);
                i = end;
                continue;
            }
            if ((c == '{') || (c == '}') || (c == ';')) {
                // A region that ends a declaration (with ';' or '}') inside a block is a
                // declaration; one that ends with '{', or is at the top level, is a
                // selector or an at-rule prelude.
                boolean declaration = (depth > 0) && (c != '{');
                out.append(declaration ? replaced : raw);
                out.append(c);
                raw.setLength(0);
                replaced.setLength(0);
                if (c == '{')
                    depth++;
                else if ((c == '}') && (depth > 0))
                    depth--;
                i++;
                continue;
            }
            if (numberStart(content, i)) {
                int j = scanNumber(content, i);
                int k = j;
                while ((k < n) && letter(content.charAt(k)))
                    k++;
                String unit = content.substring(j, k).toLowerCase();
                raw.append(content, i, k);
                if ((k > j) && UNITS.contains(unit) && ((k == n) || !identChar(content.charAt(k)))) {
                    String placeholder = PLACEHOLDER_PREFIX + placeholders.size() + ")";
                    placeholders.put(placeholder, content.substring(i, k));
                    replaced.append(placeholder);
                } else {
                    replaced.append(content, i, k);
                }
                i = k;
                continue;
            }
            raw.append(c);
            replaced.append(c);
            i++;
        }
        // Trailing content (with no terminator) is not a declaration.
        out.append(raw);
        return out.toString();
    }

    /**
     * Replaces the placeholders created by {@link #protect(String, Map)} with the
     * dimensions they stand for.
     *
     * @param content
     *                     the content (i.e. the parser's output).
     * @param placeholders
     *                     the placeholders.
     * @return the restored content.
     */
    public static String restore(String content, Map<String, String> placeholders) {
        if ((content == null) || placeholders.isEmpty() || !content.contains(PLACEHOLDER_PREFIX))
            return content;
        for (Map.Entry<String, String> entry : placeholders.entrySet())
            content = content.replace(entry.getKey(), entry.getValue());
        return content;
    }

    /**
     * Whether a number starts at {@code i} (and is not part of an identifier, a
     * hash or another number).
     */
    private static boolean numberStart(String content, int i) {
        char c = content.charAt(i);
        boolean digitNext = ((i + 1) < content.length()) && digit(content.charAt(i + 1));
        if (!digit(c) && !((c == '.') && digitNext))
            return false;
        if (i == 0)
            return true;
        char p = content.charAt(i - 1);
        if ((p == '-') || (p == '+')) {
            // A sign: what comes before it must not make it part of an identifier.
            return (i < 2) || !identChar(content.charAt(i - 2));
        }
        return !identChar(p) && (p != '.') && (p != '#') && (p != '\\');
    }

    /**
     * Scans a number (digits, an optional fraction and an optional exponent)
     * starting at {@code i}, returning the index after it.
     */
    private static int scanNumber(String content, int i) {
        int n = content.length();
        int j = i;
        while ((j < n) && digit(content.charAt(j)))
            j++;
        if (((j + 1) < n) && (content.charAt(j) == '.') && digit(content.charAt(j + 1))) {
            j++;
            while ((j < n) && digit(content.charAt(j)))
                j++;
        }
        if ((j < n) && ((content.charAt(j) == 'e') || (content.charAt(j) == 'E'))) {
            int k = j + 1;
            if ((k < n) && ((content.charAt(k) == '+') || (content.charAt(k) == '-')))
                k++;
            if ((k < n) && digit(content.charAt(k))) {
                j = k;
                while ((j < n) && digit(content.charAt(j)))
                    j++;
            }
        }
        return j;
    }

    private static boolean digit(char c) {
        return (c >= '0') && (c <= '9');
    }

    private static boolean letter(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    private static boolean identChar(char c) {
        return letter(c) || digit(c) || (c == '_') || (c == '-') || (c >= 0x80);
    }
}
