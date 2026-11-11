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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The rule for which link addresses may be drawn as a link (an anchor's
 * {@code href}) or opened, shared by the formatted text renderers, the editor
 * and {@link LinkHandlers}.
 * <p>
 * An address is safe when it has no scheme (a relative path, {@code //host/...}
 * or an in-page {@code #anchor}) or its scheme is one of {@code http},
 * {@code https}, {@code mailto} or {@code tel}. Anything else (notably
 * {@code javascript:}, {@code vbscript:} and {@code data:}) is refused: a
 * renderer draws the link's text without a link and a link handler does not
 * open it.
 * <p>
 * The address is read as a browser reads it (see the WHATWG URL standard):
 * leading and trailing C0 control characters and spaces are ignored, tabs and
 * newlines anywhere are ignored, and the scheme is case insensitive. So
 * {@code " JaVa\tScRiPt:alert(1)"} is a {@code javascript:} address.
 * <p>
 * An application that routes its own schemes through a link handler (for
 * example {@code doc:} via {@link LinkHandlers#standard(ILinkHandler)}) can
 * allow them with {@link #allow(String...)}; {@code javascript}, {@code vbscript}
 * and {@code data} can't be allowed.
 */
public final class SafeLinks {

    /**
     * The schemes that are always safe.
     */
    private static final Set<String> SAFE = new HashSet<>(Arrays.asList("http", "https", "mailto", "tel"));

    /**
     * The schemes that can never be allowed.
     */
    private static final Set<String> NEVER = new HashSet<>(Arrays.asList("javascript", "vbscript", "data"));

    /**
     * Additional schemes allowed by the application (see {@link #allow(String...)}).
     */
    private static final Set<String> ALLOWED = new HashSet<>();

    private SafeLinks() {
        // Static helpers only.
    }

    /**
     * Allows additional schemes (application-wide), for addresses that the
     * application's link handler resolves itself (for example {@code doc:}).
     * Schemes are case insensitive.
     *
     * @param schemes
     *                the schemes (without the trailing colon).
     * @throws IllegalArgumentException
     *                                  if a scheme is not a valid scheme or is one
     *                                  that can't be allowed ({@code javascript},
     *                                  {@code vbscript} or {@code data}).
     */
    public static void allow(String... schemes) {
        for (String scheme : schemes) {
            String lower = (scheme == null) ? null : scheme.toLowerCase();
            if ((lower == null) || !validScheme(lower, lower.length()))
                throw new IllegalArgumentException("Not a scheme: " + scheme);
            if (NEVER.contains(lower))
                throw new IllegalArgumentException("Scheme can't be allowed: " + scheme);
            ALLOWED.add(lower);
        }
    }

    /**
     * Removes any schemes allowed with {@link #allow(String...)} (used by tests).
     */
    static void reset() {
        ALLOWED.clear();
    }

    /**
     * Determines if an address may be drawn as a link or opened.
     *
     * @param url
     *            the address (may be {@code null}).
     * @return {@code true} if the address is safe (a {@code null} address is
     *         not).
     */
    public static boolean isSafe(String url) {
        if (url == null)
            return false;
        String scheme = scheme(url);
        if (scheme == null)
            return true;
        return SAFE.contains(scheme) || ALLOWED.contains(scheme);
    }

    /**
     * Returns the address when it is safe, otherwise {@code null}.
     *
     * @param url
     *            the address (may be {@code null}).
     * @return the address or {@code null}.
     */
    public static String safe(String url) {
        return isSafe(url) ? url : null;
    }

    /**
     * Determines if an address is an external one: its scheme is {@code http},
     * {@code https}, {@code mailto} or {@code tel} (read as a browser reads it).
     *
     * @param url
     *            the address (may be {@code null}).
     * @return {@code true} if it is.
     */
    public static boolean isExternal(String url) {
        if (url == null)
            return false;
        String scheme = scheme(url);
        return (scheme != null) && SAFE.contains(scheme);
    }

    /**
     * The scheme of an address, read as a browser reads it.
     *
     * @param url
     *            the address (may be {@code null}).
     * @return the scheme in lower case, or {@code null} if the address has none
     *         (it is relative).
     */
    public static String scheme(String url) {
        if (url == null)
            return null;
        String normalised = normalise(url);
        int colon = normalised.indexOf(':');
        if (colon <= 0)
            return null;
        if (!validScheme(normalised, colon))
            return null;
        return normalised.substring(0, colon).toLowerCase();
    }

    /**
     * Normalises an address as a browser does before parsing it: removes leading
     * and trailing C0 control characters and spaces, and removes tabs and
     * newlines wherever they are.
     *
     * @param url
     *            the address.
     * @return the normalised address.
     */
    public static String normalise(String url) {
        if (url == null)
            return null;
        int start = 0;
        int end = url.length();
        while ((start < end) && (url.charAt(start) <= ' '))
            start++;
        while ((end > start) && (url.charAt(end - 1) <= ' '))
            end--;
        StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = url.charAt(i);
            if ((c != '\t') && (c != '\n') && (c != '\r'))
                sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Whether the first {@code length} characters of {@code str} form a scheme:
     * an ASCII letter followed by ASCII letters, digits, {@code +}, {@code -} or
     * {@code .}.
     */
    private static boolean validScheme(String str, int length) {
        if (length <= 0)
            return false;
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            boolean alpha = ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
            if (i == 0) {
                if (!alpha)
                    return false;
                continue;
            }
            boolean other = ((c >= '0') && (c <= '9')) || (c == '+') || (c == '-') || (c == '.');
            if (!alpha && !other)
                return false;
        }
        return true;
    }
}
