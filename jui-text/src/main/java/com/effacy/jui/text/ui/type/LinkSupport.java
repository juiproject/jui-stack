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

import elemental2.dom.Element;
import elemental2.dom.Event;
import jsinterop.base.Js;

/**
 * Click plumbing that routes activation of a link within a content root through an
 * {@link ILinkHandler}. Used by both the editor and the read-only renderer / {@code FText}
 * so link behaviour is defined once, in one shape.
 * <p>
 * {@link #bind(Element, ILinkHandler)} attaches a single delegated listener to a content root
 * (the usual case for read-only content that renders into a caller-owned element). Regardless
 * of the handler's outcome, an in-page {@code #anchor} click always has its default suppressed,
 * so it can never reach a single-page-app hash router.
 */
public final class LinkSupport {

    private LinkSupport() {
        // Static helpers only.
    }

    /**
     * Attaches a delegated click listener to a content root that routes link activations through
     * the given handler. Safe to call once on a persistent root; it survives content re-renders.
     *
     * @param root
     *              the content root element.
     * @param handler
     *              the link handler (typically {@link LinkHandlers#standard()} or a composition).
     */
    public static void bind(Element root, ILinkHandler handler) {
        if (root == null)
            return;
        root.addEventListener ("click", evt -> handleClick (evt, root, handler));
    }

    /**
     * Handles a click event within a content root: if it falls on (or within) a link, the handler
     * is consulted and the browser's default navigation is suppressed when the handler acts — and
     * always for an in-page {@code #anchor} (SPA safety).
     *
     * @param evt
     *              the click event.
     * @param root
     *              the content root (bounds the anchor search and the in-page-anchor scope).
     * @param handler
     *              the link handler (may be {@code null}).
     */
    public static void handleClick(Event evt, Element root, ILinkHandler handler) {
        Element target = Js.uncheckedCast (evt.target);
        Element anchor = anchorAncestor (target, root);
        if (anchor == null)
            return;
        String url = anchor.getAttribute ("href");
        boolean handled = (handler != null) && handler.activate (url, anchor, root);
        if (handled || LinkHandlers.isInPageAnchor (url)) {
            // Suppress the browser's default navigation, and stop the click bubbling to any
            // surrounding handler (e.g. a click-to-edit surface) — activating a link should not
            // also do whatever a bare click on the content would.
            evt.preventDefault ();
            evt.stopPropagation ();
        }
    }

    /**
     * The nearest ancestor {@code <a href>} of {@code el}, up to (and not beyond) {@code root}.
     *
     * @param el
     *             the element to start from.
     * @param root
     *             the boundary (search stops here); may be {@code null} to search to the document.
     * @return the anchor element, or {@code null}.
     */
    public static Element anchorAncestor(Element el, Element root) {
        Element cur = el;
        while ((cur != null) && (cur != root)) {
            if ("A".equalsIgnoreCase (cur.tagName) && cur.hasAttribute ("href"))
                return cur;
            cur = cur.parentElement;
        }
        return null;
    }
}
