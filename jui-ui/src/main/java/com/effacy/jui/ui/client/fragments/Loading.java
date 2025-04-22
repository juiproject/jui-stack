package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;

/**
 * Displays a loading indicator (which is a block with slightly rounded corners
 * and that "pulses").
 * <p>
 * This can be used to compose a temporary loading variant of a presentation
 * while data is being retrieved. To facilitate the dynamic arrangement of sizes
 * width, height and margins can be set explicitly (rather than through
 * adornments).
 * <p>
 * For reference the background effect is implemented with the CSS
 * <code>animation: jui-animation-waiting 1s ease-in infinite;</code>.
 */
public class Loading {

    public static LoadingFragment $() {
        return new LoadingFragment ();
    }

    public static LoadingFragment $(IDomInsertableContainer<?> parent) {
        LoadingFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * The fragment implementation.
     */
    public static class LoadingFragment extends BaseFragment<LoadingFragment> {

        /**
         * See {@link #dark()}.
         */
        private boolean dark;

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #height(Length)}.
         */
        private Length height;

        /**
         * See {@link #top(Length)}.
         */
        private Length top;

        /**
         * See {@link #bottom(Length)}.
         */
        private Length bottom;

        /**
         * See {@link #top(Length)}.
         */
        private Length left;

        /**
         * See {@link #bottom(Length)}.
         */
        private Length right;

        /**
         * Darken the loading indicator (generally for use when there is a background).
         * <p>
         * Alternatively set a background via {@link #css(String)}.
         * 
         * @return this fragment.
         */
        public LoadingFragment dark() {
            this.dark = true;
            return this;
        }

        /**
         * Assigns a specific width.
         * 
         * @param width
         *              the width.
         * @return this fragment.
         */
        public LoadingFragment width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Assigns a specific height.
         * 
         * @param height
         *              the height.
         * @return this fragment.
         */
        public LoadingFragment height(Length height) {
            this.height = height;
            return this;
        }

        /**
         * Assigns a specific height and width.
         * 
         * @param height
         *              the height.
         * @param width
         *              the width.
         * @return this fragment.
         */
        public LoadingFragment size(Length height, Length width) {
            this.height = height;
            this.width = width;
            return this;
        }

        /**
         * Assigns a specific top margin.
         * 
         * @param top
         *              the margin.
         * @return this fragment.
         */
        public LoadingFragment top(Length top) {
            this.top = top;
            return this;
        }

        /**
         * Assigns a specific bottom margin.
         * 
         * @param bottom
         *              the margin.
         * @return this fragment.
         */
        public LoadingFragment bottom(Length bottom) {
            this.bottom = bottom;
            return this;
        }

        /**
         * Assigns a specific left margin.
         * 
         * @param left
         *              the margin.
         * @return this fragment.
         */
        public LoadingFragment left(Length left) {
            this.left = left;
            return this;
        }

        /**
         * Assigns a specific right margin.
         * 
         * @param right
         *              the margin.
         * @return this fragment.
         */
        public LoadingFragment right(Length right) {
            this.right = right;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            return Div.$ (parent).style ("juiLoading").$ (item -> {
                if (dark)
                    item.style("juiLoading_dark");
                if (width != null) {
                    item.css(CSS.WIDTH, width);
                    item.css(CSS.MIN_WIDTH, width);
                    item.css(CSS.MAX_WIDTH, width);
                }
                if (height != null) {
                    item.css(CSS.HEIGHT, height);
                    item.css(CSS.MIN_HEIGHT, height);
                    item.css(CSS.MAX_HEIGHT, height);
                }
                if (top != null)
                    item.css(CSS.MARGIN_TOP, top);
                if (bottom != null)
                    item.css(CSS.MARGIN_BOTTOM, bottom);
                if (left != null)
                    item.css(CSS.MARGIN_LEFT, left);
                if (right != null)
                    item.css(CSS.MARGIN_RIGHT, right);
            });
        }
    }

}
