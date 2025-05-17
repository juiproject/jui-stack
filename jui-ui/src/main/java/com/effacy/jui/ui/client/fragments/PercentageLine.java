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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Displays a horizontal completion bar guaged by percentage (0-100). Percentage
 * is displayed to the right or above (with additional text).
 */
public class PercentageLine extends BaseFragment<PercentageGuage> {

    /**
     * Construct guage with the given percentage.
     * 
     * @param percentage
     *                   the percentage to display (from 0 - 100).
     * @return the guage instance.
     */
    public static PercentageLine $(int percentage) {
        return $ (null, percentage);
    }

    /**
     * Construct guage with the given percentage.
     * 
     * @param parent
     *                   the parent to insert into.
     * @param percentage
     *                   the percentage to display (from 0 - 100).
     * @return the guage instance.
     */
    public static PercentageLine $(IDomInsertableContainer<?> parent, int percentage) {
        PercentageLine frg = new PercentageLine (percentage);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Construct guage with the calculated percentage.
     * 
     * @param parent
     *                    the parent to insert into.
     * @param numerator
     *                    the numerator to the percentage fraction.
     * @param denominator
     *                    the denominator to the percentage fraction (if zero then
     *                    taken as no progress).
     * @return the guage instance.
     */
    public static PercentageLine $(IDomInsertableContainer<?> parent, int numerator, int denominator) {
        int percentage = 0;
        if ((denominator > 0) && (numerator > 0))
            percentage = Double.valueOf(100.0 * ((double) numerator) / ((double) denominator)).intValue();
        if (percentage > 100)
            percentage = 100;
        return $ (parent, percentage);
    }

    /**
     * The percentage to display.
     */
    private int percentage;

    /**
     * See {@link #label(String)}
     */
    private String label;

    /**
     * See {@link #progress(String)}.
     */
    private String progress;

    /**
     * Construct with a percentage to display.
     * 
     * @param percentage
     *                   the percentage (from 0 - 100).
     */
    public PercentageLine(int percentage) {
        this.percentage = percentage;
    }

    /**
     * Assigns the display label to use. If not present then the percentage is
     * rendered to the right of the bar.
     * 
     * @param label
     *              the label.
     * @return this fragment instance.
     */
    public PercentageLine label(String label) {
        this.label = label;
        return this;
    }

    /**
     * See {@link #progress(boolean, String)} but with no condition.
     * 
     * @param progress
     *                 the progress text.
     * @return this fragment instance.
     */
    public PercentageLine progress(String progress) {
        this.progress = progress;
        return this;
    }

    /**
     * Provides alternative text for the percentage progress.
     * 
     * @param condition
     *                  a condition that must hold for the progress text to apply.
     * @param progress
     *                  the progress text.
     * @return this fragment instance.
     */
    public PercentageLine progress(boolean condition, String progress) {
        if (condition)
            this.progress = progress;
        return this;
    }

    @Override
    protected void buildInto(ElementBuilder root) {
        root.style("juiPLine");
        int per = (percentage < 0) ? 0 : ((percentage > 100) ? 100 : percentage);
        Div.$ (root).$ (line -> {
            if (!StringSupport.empty(label)) {
                Div.$(line).style("info").$(info -> {
                    Span.$(info).text (label);
                    Span.$(info).css("flex-grow: 1;");
                    if (!StringSupport.empty(progress))
                        Div.$(info).style("info").text(progress);
                    else
                        Div.$(info).style("info").text("" + per + "%");
                });
            }
            Div.$(line).style("wrap").$(bar -> {
                Div.$(bar).style("bar").$ (
                    Div.$().css(CSS.WIDTH, Length.pct(per))
                );
                if (StringSupport.empty(label)) {
                    if (!StringSupport.empty(progress))
                        Div.$(bar).style("info").text(progress);
                    else
                        Div.$(bar).style("info").text("" + per + "%");
                }
            });
        });
    }
}

