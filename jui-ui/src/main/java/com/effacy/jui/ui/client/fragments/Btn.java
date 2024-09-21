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

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;

public class Btn {
    public static BtnFragment $(String label) {
        return new BtnFragment (label);
    }

    public static BtnFragment $(IDomInsertableContainer<?> parent, String label) {
        BtnFragment frg = $ (label);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public enum Variant {
        /**
         * Standard button presentation.
         */
        STANDARD,
        
        /**
         * Same as {@link #STANDARD} but expands the padding.
         */
        STANDARD_EXPANDED,
        
        /**
         * Draws with an outline.
         */
        OUTLINED,
        
        /**
         * Text only (link-like).
         */
        TEXT;
    }

    public enum Nature {
        NORMAL, WARNING, DANGER, SUCCESS;
    }

    public static class BtnFragment extends BaseFragment<BtnFragment> {

        /**
         * See constructor.
         */
        private String label;

        /**
         * See {@link #variant(Variant)}.
         */
        private Variant variant = Variant.STANDARD;

        /**
         * See {@link #nature(Nature)}.
         */
        private Nature nature = Nature.NORMAL;

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #size(Length)}.
         */
        private Length size;

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #compact(boolean)}.
         */
        private boolean compact;

        /**
         * See {@link #onclick(Invoker)}.
         */
        private Invoker onclick;

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * Construct with the label for the button.
         * 
         * @param label
         *             the label.
         */
        public BtnFragment(String label) {
            this.label = label;
        }

        /**
         * The button variant.
         * 
         * @param variant
         *              the variant to apply.
         * @return the fragment instance.
         */
        public BtnFragment variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return this;
        }

        /**
         * The button nature.
         * 
         * @param nature
         *              the nature to apply.
         * @return the fragment instance.
         */
        public BtnFragment nature(Nature nature) {
            if (nature != null)
                this.nature = nature;
            return this;
        }

        /**
         * Declares an icon to display.
         * 
         * @param icon
         *              the icon CSS to apply.
         * @return the fragment instance.
         */
        public BtnFragment icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * The font size.
         * 
         * @param size
         *              the size to apply.
         * @return the fragment instance.
         */
        public BtnFragment size(Length size) {
            this.size = size;
            return this;
        }

        /**
         * See {@link #compact(boolean)}. Convenience to pass {@code true}.
         */
        public BtnFragment compact() {
            return compact (true);
        }

        /**
         * To render in a compact form.
         * 
         * @param compact
         *              {@code true} if to render in compact form.
         * @return the fragment instance.
         */
        public BtnFragment compact(boolean compact) {
            this.compact = compact;
            return this;
        }

        /**
         * The width.
         * 
         * @param width
         *              the width to apply.
         * @return the fragment instance.
         */
        public BtnFragment width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public BtnFragment onclick(Invoker onclick) {
            this.onclick = onclick;
            return this;
        }

        /**
         * Assigns a test ID to the action.
         * 
         * @param testId
         *                test ID.
         * @return this icon instance.
         */
        public BtnFragment testId(String testId) {
            this.testId = testId;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (label == null)
                return null;
            ElementBuilder btn = com.effacy.jui.core.client.dom.builder.Button.$ (parent);
            if (!StringSupport.empty(icon))
                Em.$ (btn).style (icon);
            if (testId != null)
                btn.testId (testId);
            btn.text (label);
            btn.style ("juiButton", "juiButton-" + variant.name ().toLowerCase (), "juiButton-" + nature.name ().toLowerCase ());
            if (compact)
                btn.style ("compact");
            if (size != null)
                btn.css (CSS.FONT_SIZE, size);
            if (width != null)
                btn.css (CSS.WIDTH, width);
            if (onclick != null)
                btn.onclick (e -> onclick.invoke());
            return btn;
        }

    }
}
