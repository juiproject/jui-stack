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
package com.effacy.jui.core.client.dom.builder;

import com.effacy.jui.core.client.dom.builder.Fragment.IFragmentAdornment;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;

/**
 * Collection of stanard fragment adornments.
 */
public final class FragmentAdornments {

    public static IFragmentAdornment css(String css) {
        return (el) -> el.css (css);
    }

    public static IFragmentAdornment style(String... styles) {
        return (el) -> el.style (styles);
    }
    
    public static IFragmentAdornment fontSize(Length size) {
        return (el) -> el.css (CSS.FONT_SIZE, size);
    }
    
    public static IFragmentAdornment height(Length height) {
        return (el) -> el.css (CSS.HEIGHT, height);
    }
    
    public static IFragmentAdornment minHeight(Length height) {
        return (el) -> el.css (CSS.MIN_HEIGHT, height);
    }
    
    public static IFragmentAdornment width(Length width) {
        return (el) -> el.css (CSS.WIDTH, width);
    }
    
    public static IFragmentAdornment minWidth(Length width) {
        return (el) -> el.css (CSS.MIN_WIDTH, width);
    }
    
    public static IFragmentAdornment padding(Insets padding) {
        return (el) -> el.css (CSS.PADDING, padding);
    }

    public static IFragmentAdornment margin(Insets margin) {
        return (el) -> el.css (CSS.MARGIN, margin);
    }

    public static IFragmentAdornment marginRight(Length margin) {
        return (el) -> el.css (CSS.MARGIN_RIGHT, margin);
    }

    public static IFragmentAdornment marginTop(Length margin) {
        return (el) -> el.css (CSS.MARGIN_TOP, margin);
    }

    public static IFragmentAdornment marginBottom(Length margin) {
        return (el) -> el.css (CSS.MARGIN_BOTTOM, margin);
    }

    public static IFragmentAdornment marginLeft(Length margin) {
        return (el) -> el.css (CSS.MARGIN_LEFT, margin);
    }

    public static IFragmentAdornment grow(int extent) {
        return (el) -> el.css ("flexGrow", "" + extent);
    }

    public static IFragmentAdornment color(Color color) {
        return (el) -> el.css (CSS.COLOR, color);
    }
}
