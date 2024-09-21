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
package com.effacy.jui.core.client.dom.css;

import com.effacy.jui.core.client.dom.css.CSS.CSSPropertyApplier;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Element;

/**
 * Represents a CSS border.
 *
 * @author Jeremy Buckley
 */
public class BorderEdge implements CSSPropertyApplier {

    /**
     * Construct border (with pixel sizing).
     * 
     * @param width
     *              the border width.
     * @param style
     *              the border style.
     * @param color
     *              the border color.
     * @return the border.
     */
    public static BorderEdge line(int width, BorderStyle style, String color) {
        return new BorderEdge (Length.px (width), style, color);
    }

    /**
     * See {@link #color(String)}.
     */
    private String color;

    /**
     * See {@link #style(BorderStyle)}.
     */
    private BorderStyle style;

    /**
     * See {@link #width(Length)}.
     */
    private Length width;

    /**
     * Construct a border line.
     * 
     * @param width
     *              the border width.
     * @param style
     *              the border style.
     * @param color
     *              the border color.
     */
    public BorderEdge(Length width, BorderStyle style, String color) {
        this.width = width;
        this.style = style;
        this.color = color;
    }

    /**
     * Assigns a border width.
     * 
     * @param width
     *              the width.
     * @return this border instance.
     */
    public BorderEdge width(Length width) {
        this.width = width;
        return this;
    }

    /**
     * Assigns a border style.
     * 
     * @param style
     *              the style.
     * @return this border instance.
     */
    public BorderEdge style(BorderStyle style) {
        this.style = style;
        return this;
    }

    /**
     * Assigns a border color.
     * 
     * @param color
     *              the color.
     * @return this border instance.
     */
    public BorderEdge color(String color) {
        this.color = color;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.css.CSS.CSSPropertyApplier#apply(com.effacy.jui.core.client.dom.renderer.DOMDataRenderer.Element)
     */
    @Override
    public <E extends Element<?>> E apply(E el) {
        if (width != null)
            el.css ("borderWidth", width.value ());
        if (color != null)
            el.css ("borderColor", color);
        if (style != null)
            el.css ("borderStyle", style.value ());
        return el;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.css.CSS.CSSPropertyApplier#apply(JQueryElement)
     */
    @Override
    public JQueryElement apply(JQueryElement el) {
        if (width != null)
            el.css ("borderWidth", width.value ());
        if (color != null)
            el.css ("borderColor", color);
        if (style != null)
            el.css ("borderStyle", style.value ());
        return el;
    }

}
