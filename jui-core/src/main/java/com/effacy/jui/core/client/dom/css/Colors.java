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
package com.effacy.jui.core.client.dom.css;

import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;

import elemental2.dom.Element;

/**
 * Way of capturing both foreground and background colors.
 */
public record Colors(Color foreground, Color background) {

    /**
     * Foreground color only.
     */
    public static Colors of(Color foreground) {
        return new Colors(foreground, null);
    }

    /**
     * Foreground color only but conditional (if fails then {@code null} is
     * returned).
     */
    public static Colors of(Color foreground, boolean condition) {
        if (!condition)
            return null;
        return new Colors(foreground, null);
    }
    
    /**
     * Foreground and background colors.
     */
    public static Colors of(Color foreground, Color background) {
        return new Colors(foreground, null);
    }
    
    /**
     * Foreground and background colors but conditional (if fails then {@code null}
     * is returned).
     */
    public static Colors of(Color foreground, Color background, boolean condition) {
        if (!condition)
            return null;
        return new Colors(foreground, background);
    }

    /**
     * Applies the colours to an element.
     */
    public Element apply(Element el) {
        apply (JQuery.$ (el));
        return el;
    }

    /**
     * Applies the colours to an element.
     */
    public JQueryElement apply(JQueryElement el) {
        if (foreground != null)
            CSS.COLOR.apply(el, foreground);
        if (background != null)
            CSS.BACKGROUND_COLOR.apply(el, background);
        return el;
    }

    /**
     * Applies the colours to an element.
     */
    public ElementBuilder apply(ElementBuilder el) {
        if (foreground != null)
            el.css (CSS.COLOR, foreground);
        if (background != null)
            el.css (CSS.BACKGROUND_COLOR, background);
        return el;
    }
}
