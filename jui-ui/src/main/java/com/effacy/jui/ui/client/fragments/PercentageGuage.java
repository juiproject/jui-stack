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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Svg;
import com.effacy.jui.core.client.dom.builder.Text;

/**
 * Inserts a dial-like guage which represents a percentage (with the exact
 * percentage in the center).
 * <p>
 * Passed should be the percentage as an integer between 0 and 100.
 * <p>
 * The sixe of the guage can be set by assigning (via adornment) a fixed width
 * (and a font size to update the size of the number).
 */
public class PercentageGuage extends BaseFragment<PercentageGuage> {

    public static PercentageGuage $(int percentage) {
        return $ (null, percentage);
    }

    public static PercentageGuage $(IDomInsertableContainer<?> parent, int percentage) {
        PercentageGuage frg = new PercentageGuage (percentage);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static PercentageGuage $(IDomInsertableContainer<?> parent, int numerator, int denominator) {
        int percentage = 0;
        if ((denominator > 0) && (numerator > 0))
            percentage = Double.valueOf (100.0 * ((double) numerator) / ((double) denominator)).intValue();
        if (percentage > 100)
            percentage = 100;
        return $ (parent, percentage);
    }

    private int percentage;

    public PercentageGuage(int percentage) {
        this.percentage = percentage;
    }

    protected void buildInto(ElementBuilder root) {
        root.style("juiPGuage");
        int per = (percentage < 0) ? 0 : ((percentage > 100) ? 100 : percentage);
        Div.$ (root).$ (
            Svg.$ ()
                .viewBox ("0 0 36 36")
                .path ("none", "M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831", p -> {
                    p.style ("guage_bg");
                    p.attr ("stroke-width", "3.8");
                })
                .path ("none", "M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831", p -> {
                    p.style ("guage_dial");
                    p.attr ("stroke-width", "3.8");
                    p.attr ("stroke-dasharray", per + ", 100");
                    p.attr ("stroke-linecap", "round");
                }),
            Div.$ ().$ (
                Span.$ ().$ (
                    Text.$ ("" + percentage),
                    I.$ ().text ("%")
                )
            )
        );
    }
}

