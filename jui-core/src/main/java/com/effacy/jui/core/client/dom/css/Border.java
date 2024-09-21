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


import com.effacy.jui.core.client.dom.jquery.JQueryElement;


/**
 * Represents a CSS border.
 *
 * @author Jeremy Buckley
 */
public class Border extends BorderEdge {

    /**
     * Construct border (with pixel sizing).
     * 
     * @param width
     *            the border width.
     * @param style
     *            the border style.
     * @param color
     *            the border color.
     * @param radius
     *            the border radius.
     * @return the border.
     */
    public static Border border(int width, BorderStyle style, String color, int radius) {
        return new Border (Length.px (width), style, color, Length.px (radius));
    }

    /**
     * See {@link #radius(Length)}.
     */
    private Length radius;

    /**
     * Construct a border.
     * 
     * @param width
     *            the border width.
     * @param style
     *            the border style.
     * @param color
     *            the border style.
     */
    public Border(Length width, BorderStyle style, String color) {
        this (width, style, color, null);
    }


    /**
     * Construct a border.
     * 
     * @param width
     *            the border width.
     * @param style
     *            the border style.
     * @param color
     *            the border color.
     * @param radius
     *            the border radius.
     */
    public Border(Length width, BorderStyle style, String color, Length radius) {
        super (width, style, color);
        this.radius = radius;
    }


    /**
     * Assigns a border radius.
     * 
     * @param radius
     *            the radius.
     * @return this border instance.
     */
    public Border radius(Length radius) {
        this.radius = radius;
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.css.CSS.CSSPropertyApplier#apply(JQueryElement)
     */
    @Override
    public JQueryElement apply(JQueryElement el) {
        super.apply (el);
        if (radius != null)
            el.css ("borderRadius", radius.value ());
        return el;
    }

}
