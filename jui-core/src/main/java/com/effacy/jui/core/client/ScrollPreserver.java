package com.effacy.jui.core.client;

import elemental2.dom.Element;

/**
 * Used to preserve the scroll position of an element when the element is
 * re-written.
 */
public class ScrollPreserver {

    /**
     * The element being scrolled.
     */
    private Element el;

    /**
     * The last preserved scroll top.
     */
    private double scrollTop = 0;

    /**
     * Construct with element to preserve the scrolling position of.
     * 
     * @param el
     *           the element.
     */
    public ScrollPreserver(Element el) {
        this.el = el;
    }

    /**
     * Clears the last preserved scrolling position.
     */
    public void clear() {
        this.scrollTop = 0;
    }

    /**
     * Preserves the current scrolling position.
     */
    public void preserve() {
        this.scrollTop = el.scrollTop;
    }

    /**
     * Restores the last recorded scrolling position.
     */
    public void restore() {
        el.scrollTop = this.scrollTop;
    }
}
