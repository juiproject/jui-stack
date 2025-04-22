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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.FragmentAdornments;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.fragments.Paper.APaperFragment;

import elemental2.dom.Element;

public class Card {

    public static CardFragment $() {
        return new CardFragment ();
    }

    public static CardFragment $(IDomInsertableContainer<?> parent) {
        CardFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

     /**
      * Defines a variant for a card.
      */
    public interface Variant {

        /**
         * Outlined card.
         */
        public static final Variant OUTLINED = Variant.create("variant-outlined");

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style) {
            return new Variant() {
                public String style() { return style; }
            };
        }
    }

    public static class CardFragment extends ACardFragment<CardFragment> {}

    public static class ACardFragment<T extends ACardFragment<T>> extends APaperFragment<T> {

        /**
         * See {@link #variant(Variant)}.
         */
        protected Variant variant = Variant.OUTLINED;

        /**
         * See {@link #padding(Insets)}.
         */
        protected Insets padding;

        /**
         * See {@link #onclick(BiConsumer)}.
         */
        protected BiConsumer<UIEvent,Element> onclick;

        /**
         * See {@link #gap(Length)}.
         */
        protected Length gap;

        /**
         * See {@link #horizontal(boolean)}.
         */
        protected boolean horizontal;

        /**
         * Assigns a variant to the card.
         * 
         * @param variant
         *                the variant.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return (T) this;
        }

        public T horizontal() {
            return horizontal(true);
        }

        @SuppressWarnings("unchecked")
        public T horizontal(boolean horizontal) {
            this.horizontal = horizontal;
            return (T) this;
        }

        /**
         * Assigns padding to the interior of the card.
         * 
         * @param padding
         *                the padding to apply.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T padding(Insets padding) {
            this.padding = padding;
            return (T) this;
        }

        /**
         * Assigns a width to the card.
         * 
         * @param width
         *                the width (the default is no width).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T width(Length width) {
            if (width != null)
                adorn (FragmentAdornments.width (width));
            return (T) this;
        }

        /**
         * Assigns a height to the card.
         * 
         * @param height
         *                the height (the default is no height).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T height(Length height) {
            if (height != null)
                adorn (FragmentAdornments.height (height));
            return (T) this;
        }

        /**
         * Assigns a minimum height to the card.
         * 
         * @param height
         *                the minimum height (the default is no height).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T minHeight(Length height) {
            if (height != null)
                adorn (FragmentAdornments.minHeight (height));
            return (T) this;
        }

        /**
         * Assigns a click handler for the card.
         * 
         * @param onclick
         *                the click handler.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T onclick(Invoker onclick) {
            this.onclick = (e,n) -> onclick.invoke();
            return (T) this;
        }

        /**
         * Assigns a click handler for the card.
         * 
         * @param onclick
         *                the click handler.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T onclick(Consumer<Element> onclick) {
            this.onclick = (e,n) -> onclick.accept(n);
            return (T) this;
        }

        /**
         * Assigns a click handler for the card.
         * 
         * @param onclick
         *                the click handler.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T onclick(BiConsumer<UIEvent,Element> onclick) {
            this.onclick = onclick;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T gap(Length gap) {
            this.gap = gap;
            return (T) this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiCard", variant.style ());
            if (horizontal)
                root.style ("horizontal");
            if (gap != null)
                root.css (CSS.GAP, gap);
            if (padding != null)
                root.css (CSS.PADDING, padding);
            if (onclick != null) {
                root.style ("clickable");
                root.onclick ((e, n) -> { onclick.accept (e, (Element) n);});
            }
            super.buildInto(root);
        }
    }
    
}
