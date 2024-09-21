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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Defines a menu item that appears under a {@link Menu}.
 */
public class MenuItem {

    public static MenuItemFragment $() {
        return new MenuItemFragment ();
    }

    public static MenuItemFragment $(IDomInsertableContainer<?> parent) {
        MenuItemFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Different types of menu item.
     */
    public enum Variant {
        NORMAL, ERROR;
    }

    public static class MenuItemFragment extends BaseFragment<MenuItemFragment> {

        /**
         * See {@link #variant(Variant)}.
         */
        private Variant variant = Variant.NORMAL;

        private String icon;

        /**
         * See {@link #label(String)}.
         */
        private String label;

        /**
         * See {@link #disabled(boolean)}.
         */
        private boolean disabled;

        /**
         * See {@link #disabledButClickable(boolean)}.
         */
        private boolean disabledButClickable;

        /**
         * Various on-click event handlers.
         */
        private List<Invoker> onclicks;

        /**
         * Defines the variant to render.
         * 
         * @param variant
         *                the variant.
         * @return this fragment.
         */
        public MenuItemFragment variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return this;
        }

        /**
         * Assigns an icon CSS to display.
         * <p>
         * Passing an empty string (as opposed to a {@code null}) will display an empty
         * icon.
         * 
         * @param icon
         *             the icon CSS class.
         * @return this fragment.
         */
        public MenuItemFragment icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Provides a label to display.
         * 
         * @param label
         *              the label.
         * @return this fragment.
         */
        public MenuItemFragment label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Makes the item as disabled. This will not be clickable event if an on-click
         * handler has been assigned.
         * 
         * @param disabled
         *                 {@code true} to disable.
         * @return this fragment.
         */
        public MenuItemFragment disabled(boolean disabled) {
            this.disabled = disabled;
            this.disabledButClickable = false;
            return this;
        }

        /**
         * See {@link #disabledButClickable(boolean, boolean)} but passed {@code true} to the clickable argument.
         */
        public MenuItemFragment disabledButClickable(boolean disabled) {
            return disabledButClickable (disabled, disabled);
        }

        /**
         * Similar to {@link #disabled(boolean)} but allows the item to be clickable (if
         * an on-click handler has been assigned).
         * <p>
         * The idea is to allow a click to present a message as to why the action is not
         * available.
         * 
         * @param disabled
         *                  {@code true} to disable.
         * @param clickable
         *                  {@code true} if clickable.
         * @return this fragment.
         */
        public MenuItemFragment disabledButClickable(boolean disabled, boolean clickable) {
            this.disabled = disabled;
            this.disabledButClickable = clickable;
            return this;
        }

        /**
         * Applies an on-click handler. Multiple assignments are allowed (and these are
         * additive).
         * 
         * @param onclick
         *                the handler to add.
         * @return this fragment.
         */ 
        public MenuItemFragment onclick(Invoker onclick) {
            if (onclicks == null)
                onclicks = new ArrayList<> ();
            onclicks.add (onclick);
            return this;
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            Div.$ (parent).$ (item -> {
                item.style ("juiMenuItem", "juiMenuItem-" + variant.name ().toLowerCase ());
                if (disabled)
                    item.style ("disabled");
                if (icon != null)
                    Em.$ (item).style (icon);
                if (StringSupport.empty(label))
                    Span.$ (item).text ("NO LABEL");
                else
                    Span.$ (item).text (label);
                if ((onclicks != null) && !onclicks.isEmpty () && (!disabled || disabledButClickable)) {
                    item.style ("clickable");
                    item.onclick (e -> onclicks.forEach (onclick ->  onclick.invoke ()));
                }
            });
        }
        
    }
}
