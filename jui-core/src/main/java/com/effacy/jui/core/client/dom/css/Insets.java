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

import com.effacy.jui.core.client.dom.css.CSS.ICSSProperty;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder;

import elemental2.dom.Element;

/**
 * Collection of four length values (for top, bottom, left and right).
 *
 * @author Jeremy Buckley
 */
public class Insets implements ICSSProperty {

    public static Insets em(double left, double top, double right, double bottom) {
        return new Insets (Length.em (left), Length.em (top), Length.em (right), Length.em (bottom));
    }

    public static Insets em(double lr, double tb) {
        return new Insets (Length.em (lr), Length.em (tb));
    }

    public static Insets em(double all) {
        return new Insets (Length.em (all));
    }

    public static Insets px(int left, int top, int right, int bottom) {
        return new Insets (Length.px (left), Length.px (top), Length.px (right), Length.px (bottom));
    }

    public static Insets px(int lr, int tb) {
        return new Insets (Length.px (lr), Length.px (tb));
    }

    public static Insets px(int all) {
        return new Insets (Length.px (all));
    }

    public static Insets ln(Length left, Length top, Length right, Length bottom) {
        return new Insets (left, top, right, bottom);
    }

    private Length left;

    private Length top;

    private Length right;

    private Length bottom;

    public Insets(Length all) {
        this.left = this.top = this.right = this.bottom = all;
    }

    public Insets(Length lr, Length tb) {
        this.left = this.right = lr;
        this.top = this.bottom = tb;
    }

    public Insets(Length left, Length top, Length right, Length bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public String value() {
        String value = "";
        if (this.top == null)
            value += "0";
        else
            value += this.top.value ();
        value += " ";
        if (this.right == null)
            value += "0";
        else
            value += this.right.value ();
        value += " ";
        if (this.bottom == null)
            value += "0";
        else
            value += this.bottom.value ();
        value += " ";
        if (this.left == null)
            value += "0";
        else
            value += this.left.value ();
        return value;
    }

    public Length getLeft() {
        return left;
    }

    public Length getTop() {
        return top;
    }

    public Length getRight() {
        return right;
    }

    public Length getBottom() {
        return bottom;
    }

    /**
     * Applies the insets as padding against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets padding(Element el) {
        if (left != null)
            CSS.PADDING_LEFT.with (left).apply (el);
        if (top != null)
            CSS.PADDING_TOP.with (top).apply (el);
        if (right != null)
            CSS.PADDING_RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.PADDING_BOTTOM.with (bottom).apply (el);
        return this;
    }

    /**
     * Applies the insets as padding against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets padding(TemplateBuilder.Element<?> el) {
        if (left != null)
            CSS.PADDING_LEFT.with (left).apply (el);
        if (top != null)
            CSS.PADDING_TOP.with (top).apply (el);
        if (right != null)
            CSS.PADDING_RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.PADDING_BOTTOM.with (bottom).apply (el);
        return this;
    }

    /**
     * Applies the insets as a margin against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets margin(Element el) {
        if (left != null)
            CSS.MARGIN_LEFT.with (left).apply (el);
        if (top != null)
            CSS.MARGIN_TOP.with (top).apply (el);
        if (right != null)
            CSS.MARGIN_RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.MARGIN_BOTTOM.with (bottom).apply (el);
        return this;
    }

    /**
     * Applies the insets as a margin against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets margin(TemplateBuilder.Element<?> el) {
        if (left != null)
            CSS.MARGIN_LEFT.with (left).apply (el);
        if (top != null)
            CSS.MARGIN_TOP.with (top).apply (el);
        if (right != null)
            CSS.MARGIN_RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.MARGIN_BOTTOM.with (bottom).apply (el);
        return this;
    }

    /**
     * Applies the insets as a position (top-left-bottom-right) against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets postion(Element el) {
        if (left != null)
            CSS.LEFT.with (left).apply (el);
        if (top != null)
            CSS.TOP.with (top).apply (el);
        if (right != null)
            CSS.RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.BOTTOM.with (bottom).apply (el);
        return this;
    }

    /**
     * Applies the insets as a position (top-left-bottom-right) against the element.
     * 
     * @param el
     *           the element.
     * @return this insets instance.
     */
    public Insets postion(TemplateBuilder.Element<?> el) {
        if (left != null)
            CSS.LEFT.with (left).apply (el);
        if (top != null)
            CSS.TOP.with (top).apply (el);
        if (right != null)
            CSS.RIGHT.with (right).apply (el);
        if (bottom != null)
            CSS.BOTTOM.with (bottom).apply (el);
        return this;
    }

}
