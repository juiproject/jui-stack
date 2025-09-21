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
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.fragments.MenuItem.MenuItemFragment;
import com.effacy.jui.ui.client.fragments.Paper.APaperFragment;

public class Menu {

    public static MenuFragment $() {
        return new MenuFragment ();
    }

    public static MenuFragment $(IDomInsertableContainer<?> parent) {
        MenuFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    /**
     * Different (but standard) variations of style for the menu.
     */
    public enum Variant {
        OUTLINED;
    }

    public static class MenuFragment extends AMenuFragment<MenuFragment> {}

    public static class AMenuFragment<T extends AMenuFragment<T>> extends APaperFragment<T> {

        /**
         * See {@link #variant(Variant)}.
         */
        protected Variant variant = Variant.OUTLINED;

        /**
         * See {@link #width(Length)}.
         */
        protected Length width;

        /**
         * See {@link #height(Length)}.
         */
        protected Length height;

        /**
         * Handler for when there is a click on the menu.
         * <p>
         * This is used directly by {@link MenuActivator} to close when an item is
         * clicked on.
         */
        protected Invoker menuItemClicked;

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

        /**
         * Assigns a width to the card.
         * 
         * @param width
         *                the width (the default is no width).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T width(Length width) {
            this.width = width;
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
            this.height = height;
            return (T) this;
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            if ((conditional != null) && !conditional.get())
                return;
            Ul.$ (parent).$ (ul -> {
                adorn (ul);
                ul.style ("juiMenu", "juiMenu-" + variant.name ().toLowerCase ());
                if (width != null)
                    ul.css (CSS.WIDTH, width);
                if (height != null)
                    ul.css (CSS.HEIGHT, height);
                children.forEach (child -> {
                    Li.$ (ul).$ (li -> {
                        li.insert (child);
                    });
                });
            });
        }

        @Override
        protected void onInsertChild(IDomInsertable child) {
            if (child instanceof MenuItemFragment) {
                ((MenuItemFragment)child).onclick (() -> {
                    if (menuItemClicked != null)
                        menuItemClicked.invoke ();
                });
            }
        }
        
    }
    
}
