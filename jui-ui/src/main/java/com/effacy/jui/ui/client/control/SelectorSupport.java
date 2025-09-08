package com.effacy.jui.ui.client.control;

import elemental2.dom.Element;

/**
 * Support methods for the display of selectors.
 * <p>
 * This will eventually need transition to JsInterop (i.e. not to use native
 * methods).
 */
public class SelectorSupport {

    /**
     * Finds the distance between the given element (bottom of) and the bottom of
     * the nearest scrolling parent element.
     * <p>
     * This is used to locate whether a selector activator will open its selector
     * below the visible portion of the page.
     * 
     * @param el
     *                 the element to test.
     * @param distance
     *                 the distance threshold.
     * @return the distance.
     */
    public static boolean withinBottomOfScroller(Element el, int distance) {
        if (el == null)
            return false;
        Element p = el;
        while (p != null) {
            if (_overflowStyle(p))
                break;
            p = p.parentElement;
        }
        if (p == null)
            return false;
        double pos1 = el.getBoundingClientRect().bottom;
        double pos2 = p.getBoundingClientRect().bottom;
        return (pos2 - pos1) < distance;
    }

    protected static native boolean _overflowStyle(Element el) /*-{
        style = $wnd.getComputedStyle(el);
        overflowY = style.overflowY || style.overflow;
        return /(auto|scroll|overlay)/.test(overflowY);
        //return overflowY;
    }-*/;
}
