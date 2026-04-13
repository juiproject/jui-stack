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
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

/**
 * A fragment to show a progress bar with percentage and optional commentary.
 */
public class ProgressBar extends Fragment<ProgressBar> {

    /************************************************************************
     * Factory methods
     ************************************************************************/
    
    public static ProgressBar.ProgressBarFragment $() {
        return $ (null);
    }

    public static ProgressBar.ProgressBarFragment $(int percentage, Length width, String commentary) {
        return $ (null, percentage, width, commentary);
    }

    public static ProgressBar.ProgressBarFragment $(IDomInsertableContainer<?> parent) {
        ProgressBar.ProgressBarFragment frg = new ProgressBar.ProgressBarFragment ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static ProgressBar.ProgressBarFragment $(IDomInsertableContainer<?> parent, int percentage, Length width, String commentary) {
        ProgressBar.ProgressBarFragment frg = new ProgressBar.ProgressBarFragment ();
        frg.commentary(commentary);
        frg.percentage(percentage);
        frg.width(width);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Convenient way to construct standard configurations of the progress bar.
     */
    public interface Variant extends IFragmentVariant<ProgressBarFragment> {

        /**
         * Standard presentation.
         */
        public static final Variant STANDARD = fragment -> {
            fragment.widthOnBar(true);
        };

        /**
         * Reverse text and bar.
         */
        public static final Variant REVERSE = fragment -> {
            fragment.css("flex-direction: row-reverse;");
            fragment.widthOnBar(true);
            fragment.percentageOnLeft(true);
        };

        /**
         * Bar above text.
         */
        public static final Variant VERTICAL = fragment -> {
            fragment.css("--juiProgressBar-bar-height: 5px; --juiProgressBar-bar-border-radius: 3px;");
            fragment.vertical(true);
        };

        /**
         * Bar above text mono.
         */
        public static final Variant VERTICAL_MONO = fragment -> {
            fragment.css("--juiProgressBar-bar-border-size: 0; --juiProgressBar-bar-height: 7px; --juiProgressBar-bar-border-radius: 3px; --juiProgressBar-bar-bg: #eeeeee; --juiProgressBar-bar-fg: var(--jui-color-success70); --juiProgressBar-indicator-color: var(--jui-color-success80); --juiProgressBar-indicator-size: 1.2em; --juiProgressBar-indicator-weight: 600;");
            fragment.vertical(true);
        };

    }

    /**
     * Fragment implementation.
     */
    public static class ProgressBarFragment extends BaseFragment<ProgressBarFragment> {

        /**
         * See {@link #barOnly(boolean)}.
         */
        private boolean barOnly;

        /**
         * See {@link #commentary(String)}.
         */
        private String commentary;

        /**
         * See {@link #percentage(int)}.
         */
        private int percentage = 0;

        /**
         * See {@link #percentageOnLeft(boolean)}.
         */
        private boolean percentageOnLeft = false;

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #widthOnBar(boolean)}.
         */
        private boolean widthOnBar = false;

        /**
         * See {@link #barHeight(Length)}.
         */
        private Length barHeight;

        /**
         * See {@link #vertical(boolean)}.
         */
        private boolean vertical;

        /**
         * Whether to show only the bar without the percentage.
         * 
         * @param barOnly
         *                whether to show only the bar.
         * @return the fragment instance.
         */
        public ProgressBarFragment barOnly(boolean barOnly) {
            this.barOnly = barOnly;
            return this;
        }

        /**
         * An optional commentary to show below the percentage.
         * 
         * @param commentary
         *                   the commentary to show.
         * @return the fragment instance.
         */
        public ProgressBarFragment commentary(String commentary) {
            this.commentary = commentary;
            return this;
        }

        /**
         * The percentage to show in the bar and as text.
         * 
         * @param percentage
         *                   the percentage to show.
         * @return the fragment instance.
         */
        public ProgressBarFragment percentage(int percentage) {
            this.percentage = percentage;
            return this;
        }

        /**
         * Displays the percentage on the left of the bar instead of the right (only for
         * horizontal variants).
         * 
         * @param percentageOnLeft
         *                         whether to display the percentage on the left.
         * @return the fragment instance.
         */
        public ProgressBarFragment percentageOnLeft(boolean percentageOnLeft) {
            this.percentageOnLeft = percentageOnLeft;
            return this;
        }

        /**
         * The maximum width to apply to the whole fragment or just the bar (see
         * {@link Variant#widthOnBar()}).
         * 
         * @param width
         *              the width to apply.
         * @return the fragment instance.
         */
        public ProgressBarFragment width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Whether to apply the width setting on the bar instead of the whole fragment.
         * 
         * @param widthOnBar
         *                   whether to apply the width setting on the bar.
         * @return the fragment instance.
         */
        public ProgressBarFragment widthOnBar(boolean widthOnBar) {
            this.widthOnBar = widthOnBar;
            return this;
        }

        /**
         * The height to apply to the bar.
         * 
         * @param height
         *              the height to apply.
         * @return the fragment instance.
         */
        public ProgressBarFragment barHeight(Length height) {
            this.barHeight = height;
            return this;
        }

        /**
         * Whether to display the bar vertically with the percentage below (instead of
         * horizontally with the percentage on the right or left).
         * 
         * @param vertical
         *                 whether to display the bar vertically.
         * @return the fragment instance.
         */
        public ProgressBarFragment vertical(boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            if (vertical)
                root.style(styles().vertical());
            if (percentageOnLeft)
                root.style(styles().reverse());

            // Set a bar height for the bar-only variant if not set explicitly, to avoid it
            // being too thin.
            if (barOnly && (barHeight == null))
                barHeight = Length.em(1);
            
            if ((width != null) && !widthOnBar)
                root.css (CSS.WIDTH, width);

            Div.$(root).style(styles().outer()).$(outer -> {
                Div.$(outer).style(styles().bar()).$ (bar -> {
                    if ((width != null) && widthOnBar) {
                        bar.css (CSS.WIDTH, width);
                        outer.css("flex: unset;");
                    }
                    if (barHeight != null)
                        bar.css(CSS.HEIGHT, barHeight);
                    Div.$ (bar).css("width: " + Math.max(0, percentage) + "%;");
                });
                if (!barOnly && !vertical) {
                    Div.$(outer).style(styles().indicator()).$ (
                        Text.$(Math.max(0, percentage) + "%")
                    );
                }
            });

            if (vertical) {
                if (!barOnly || !StringSupport.empty(commentary)) {
                    Div.$ (root).style(styles().commentary()).$(info -> {
                        if (!StringSupport.empty(commentary))
                            Div.$(info).text(commentary);
                        Div.$(info).css("flex: 1;");
                        if (!barOnly)
                            Div.$(info).style(styles().indicator()).text (Math.max(0, percentage) + "%");
                    });
                }
            } else {
                if (!StringSupport.empty(commentary))
                    Div.$(root).style(styles().commentary()).text(commentary);
            }
        }

        /**
         * Styles (made available to selection).
         */
        @Override
        protected ILocalCSS styles() {
            return LocalCSS.instance ();
        }
    }


    /********************************************************************
     * CSS
     ********************************************************************/    
    

    public static interface ILocalCSS extends IFragmentCSS {

        String vertical();

        String reverse();

        String outer();

        String bar();

        String indicator();

        String commentary();

    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource(stylesheet = """
        .fragment {
            --juiProgressBar-bar-border-color: var(--jui-color-success70);
            --juiProgressBar-bar-border-size: 1px;
            --juiProgressBar-bar-border-radius: 10px;
            --juiProgressBar-bar-bg: #fff;
            --juiProgressBar-bar-fg: var(--jui-color-success10);
            --juiProgressBar-bar-height: 100%;
            --juiProgressBar-commentary-vertical-gap: 0.5em;
            --juiProgressBar-commentary-gap: 2em;
            --juiProgressBar-commentary-color: var(--jui-color-neutral50);
            --juiProgressBar-commentary-size: 1em;
            --juiProgressBar-commentary-weight: 500;
            --juiProgressBar-indicator-gap: 1em;
            --juiProgressBar-indicator-color: var(--jui-color-neutral50);
            --juiProgressBar-indicator-size: 1em;
            --juiProgressBar-indicator-weight: 400;
        }
        .fragment {
            display: flex;
            gap: var(--juiProgressBar-commentary-gap);
            align-items: center;
        }
        .fragment.vertical {
            flex-direction: column;
            align-items: stretch;
            gap: var(--juiProgressBar-commentary-vertical-gap);
        }
        .fragment .outer {
            flex: 1;
            display: flex;
            align-items: center;
            gap: var(--juiProgressBar-indicator-gap);
        }
        .fragment.reverse .outer {
            flex-direction: row-reverse;
        }
        .fragment .outer .bar {
            flex: 1;
            border: var(--juiProgressBar-bar-border-size) solid var(--juiProgressBar-bar-border-color);
            border-radius: var(--juiProgressBar-bar-border-radius);
            overflow: hidden;
            background: var(--juiProgressBar-bar-bg);
        }
        .fragment .outer .bar > div {
            height: var(--juiProgressBar-bar-height);
            background: var(--juiProgressBar-bar-fg);
            height: 100%;
        }
        .fragment .outer .indicator {
            color: var(--juiProgressBar-indicator-color);
            font-size: var(--juiProgressBar-indicator-size);
            font-weight: var(--juiProgressBar-indicator-weight);
        }
        .fragment .commentary {
            display: flex;
            color: var(--juiProgressBar-commentary-color);
            font-size: var(--juiProgressBar-commentary-size);
            font-weight: var(--juiProgressBar-commentary-weight);
            text-wrap-mode: nowrap;
        }
        .fragment.reverse .commentary {
            flex-direction: row-reverse;
        }
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
