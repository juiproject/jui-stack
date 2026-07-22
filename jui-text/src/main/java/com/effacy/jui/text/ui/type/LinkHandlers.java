/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Standard {@link ILinkHandler} implementations and the URL helpers they build on.
 * <p>
 * {@link #standard()} handles the three common cases so an application is safe out of the box,
 * especially in a single-page app that uses the URL hash for routing:
 * <ul>
 * <li><b>External</b> ({@code http(s)://}, {@code mailto:}, {@code tel:}) — opened in a new tab.</li>
 * <li><b>In-page anchor</b> ({@code #section}) — the click is always suppressed (so it never
 * reaches an SPA hash router) and, if a matching element exists in the content, scrolled to.
 * A rendered heading carries a {@link #slug(String) slug} id, so {@code [x](#operating-scenarios)}
 * finds the "Operating scenarios" heading. If nothing matches, an optional application fallback
 * is consulted; failing that it is a safe no-op.</li>
 * <li><b>Anything else</b> (relative links, custom schemes such as {@code doc:}) — delegated to
 * the application fallback, else left to the browser.</li>
 * </ul>
 */
public final class LinkHandlers {

    private LinkHandlers() {
        // Static helpers only.
    }

    private static final ILinkHandler STANDARD = standard (null);

    /**
     * The standard handler (external → new tab; {@code #anchor} → scroll within the content,
     * SPA-safe; everything else left to the browser). See the class comment.
     */
    public static ILinkHandler standard() {
        return STANDARD;
    }

    /**
     * The standard handler with an application fallback consulted for links it does not itself
     * resolve — non-external, non-anchor URLs (e.g. a {@code doc:} reference), and in-page
     * anchors with no matching target in the content.
     *
     * @param fallback
     *                 the application's link handler (may be {@code null}).
     * @return the composed handler.
     */
    public static ILinkHandler standard(ILinkHandler fallback) {
        return (url, anchor, root) -> {
            if ((url == null) || url.isEmpty())
                return false;
            if (isInPageAnchor (url)) {
                // Scroll to the in-content target if there is one; else let the application try;
                // either way return true so an in-page anchor never reaches an SPA hash router.
                if (scrollToAnchor (root, url.substring (1)))
                    return true;
                if (fallback != null)
                    fallback.activate (url, anchor, root);
                return true;
            }
            if (isExternal (url)) {
                DomGlobal.window.open (url, "_blank");
                return true;
            }
            return (fallback != null) && fallback.activate (url, anchor, root);
        };
    }

    /** Whether a URL is an in-page anchor ({@code #...}). */
    public static boolean isInPageAnchor(String url) {
        return (url != null) && url.startsWith ("#");
    }

    /** Whether a URL is external (has an absolute scheme — {@code ://}, {@code mailto:} or {@code tel:}). */
    public static boolean isExternal(String url) {
        if (url == null)
            return false;
        return url.contains ("://") || url.startsWith ("mailto:") || url.startsWith ("tel:");
    }

    /**
     * Scrolls to the element with the given id within {@code root} (or the document when
     * {@code root} is {@code null}), if one exists and lies inside the root.
     *
     * @param root
     *             the content scope (may be {@code null}).
     * @param id
     *             the target id (an anchor's fragment without the leading {@code #}).
     * @return {@code true} if a target was found and scrolled to.
     */
    public static boolean scrollToAnchor(Element root, String id) {
        if ((id == null) || id.isEmpty ())
            return false;
        Element target = null;
        if (root != null) {
            // Search within the scope first, so the correct copy is found when the same id
            // exists elsewhere on the page (e.g. an editor and a read view of the same content).
            if (isSafeSelectorId (id))
                target = root.querySelector ("[id=\"" + id + "\"]");
            if (target == null) {
                Element byId = DomGlobal.document.getElementById (id);
                if ((byId != null) && root.contains (byId))
                    target = byId;
            }
        } else {
            target = DomGlobal.document.getElementById (id);
        }
        if (target == null)
            return false;
        target.scrollIntoView ();
        return true;
    }

    /** Whether an id is safe to embed in an {@code [id="..."]} attribute selector. */
    private static boolean isSafeSelectorId(String id) {
        return !id.contains ("\"") && !id.contains ("\\");
    }

    /**
     * Derives a GitHub-style anchor slug from heading (or other) text: lower-cased, with each
     * run of non-alphanumeric characters collapsed to a single hyphen and leading/trailing
     * hyphens trimmed. So {@code "Operating scenarios"} → {@code "operating-scenarios"}.
     *
     * @param text
     *             the source text (may be {@code null}).
     * @return the slug (never {@code null}).
     */
    public static String slug(String text) {
        if (text == null)
            return "";
        String lower = text.toLowerCase ().trim ();
        StringBuilder sb = new StringBuilder ();
        boolean pendingHyphen = false;
        for (int i = 0; i < lower.length (); i++) {
            char c = lower.charAt (i);
            if (((c >= 'a') && (c <= 'z')) || ((c >= '0') && (c <= '9'))) {
                if (pendingHyphen && (sb.length () > 0))
                    sb.append ('-');
                pendingHyphen = false;
                sb.append (c);
            } else {
                pendingHyphen = true;
            }
        }
        return sb.toString ();
    }
}
