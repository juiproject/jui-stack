package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;

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
     * The visual form that the button takes.
     */
    public interface Variant {

        /**
         * Standard button presentation.
         */
        public static final Variant STANDARD = Variant.create("variant-standard", true);

        /**
         * Standard button presentation.
         */
        public static final Variant VERTICAL = Variant.create("variant-vertical", false);

        /**
         * Standard button presentation.
         */
        public static final Variant VERTICAL_MONO = Variant.create("variant-vertical-mono", false);

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * Apply width setting on the bar (not the whole fragment).
         */
        public boolean widthOnBar();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style, boolean widthOnBar) {
            return new Variant() {
                public String style() { return style; }
                public boolean widthOnBar() { return widthOnBar; }
            };
        }
    }

    /**
     * Fragment implementation.
     */
    public static class ProgressBarFragment extends BaseFragment<ProgressBarFragment> {

        /**
         * See {@link #variant(Variant)}.
         */
        private Variant variant = Variant.STANDARD;

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
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #barHeight(Length)}.
         */
        private Length barHeight;

        /**
         * The variant.
         * 
         * @param variant
         *              the variant to apply.
         * @return the fragment instance.
         */
        public ProgressBarFragment variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return this;
        }

        /**
         * Whether to show only the bar without the percentage and commentary.
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

        @Override
        protected void buildInto(ElementBuilder root) {
            // Set a bar height for the bar-only variant if not set explicitly, to avoid it
            // being too thin.
            if (barOnly && (barHeight == null))
                barHeight = Length.em(1);
            
            root.style("juiProgressBar", variant.style());
            if ((width != null) && !variant.widthOnBar())
                root.css (CSS.MAX_WIDTH, width);
            Div.$ (root).style("juiProgressBar_bar").$ (bar -> {
                if ((width != null) && variant.widthOnBar())
                    bar.css (CSS.MAX_WIDTH, width);
                if (barHeight != null)
                    bar.css(CSS.HEIGHT, barHeight);
                Div.$ (bar).css("width: " + Math.max(0, percentage) + "%;");
            });
            if (!barOnly) {
                Div.$(root).style("juiProgressBar_info").$(info -> {
                    Div.$ (info).style("juiProgressBar_indicator").$ (
                        Text.$(Math.max(0, percentage) + "%")
                    );
                    if (!StringSupport.empty(commentary)) {
                        Div.$ (info).style("juiProgressBar_commentary").$ (
                            Text.$(commentary)
                        );
                    }
                });
            }
        }
    }

}
