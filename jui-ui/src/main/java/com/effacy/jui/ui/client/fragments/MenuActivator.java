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

import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.ui.client.fragments.Menu.MenuFragment;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

public class MenuActivator {

    public static MenuActivatorFragment $() {
        return new MenuActivatorFragment ();
    }

    public static MenuActivatorFragment $(IDomInsertableContainer<?> parent) {
        MenuActivatorFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    public static class MenuActivatorFragment extends AMenuActivatorFragment<MenuActivatorFragment> {}

    public static class AMenuActivatorFragment<T extends AMenuActivatorFragment<T>> extends BaseFragmentWithChildren<T> {

        /**
         * See {@link #clickToActivate()}.
         */
        private boolean clickToActivate;

        /**
         * See {@link #aboveThreshold(int)}.
         */
        private int aboveThreshold = 100;

        /**
         * Internal. Holds the activator.
         */
        private ActivationHandler handler;

        /**
         * The default is that the menu opens on mouse-over. However, it can be
         * configured to open on a click.
         * 
         * @return this fragment instance.
         */
        @SuppressWarnings("unchecked")
        public T clickToActivate() {
            this.clickToActivate = true;
            return (T)this;
        }

        /**
         * When the menu activator is with 100px of the bottom of the enclosing scroller
         * then menu will be displayed above the activator. This allows to to adjust
         * that threshold, but also allows you to disable it by setting a value that is
         * zero (or less).
         * <p>
         * Note that when displaying above the menu is offset above the bottom to clear
         * the height of the activator. This is configured in CSS with the variable
         * {@code --juiMenuActivator-aboveOffset}.
         * 
         * @param aboveThreshold
         *                       the activation threshold in px to situate the menu
         *                       above the activator.
         * @return this fragment instance.
         */
        @SuppressWarnings("unchecked")
        public T aboveThreshold(int aboveThreshold) {
            this.aboveThreshold = aboveThreshold;
            return (T)this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiMenuActivator");
            if (clickToActivate) {
                root.apply (n -> {
                    if (aboveThreshold > 0)
                        handler = new ActivationHandler ((Element) n, (Element) n, "open", "above", aboveThreshold);
                    else
                        handler = new ActivationHandler ((Element) n, (Element) n, "open");
                });
                root.onclick(e -> {
                    handler.toggle ();
                });
            }
            Em.$ (root).style ("activator", FontAwesome.ellipsisV ());
            Div.$ (root).$ (container -> {
                if (!clickToActivate)
                    container.style ("hoverable");
                super.buildInto (container);
            });
        }

        @Override
        protected void onInsertChild(IDomInsertable child) {
            if (child instanceof MenuFragment) {
                ((MenuFragment)child).menuItemClicked = (() -> {
                    handler.close ();
                });
            }
        }
        
    }
}
