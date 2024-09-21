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
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Length;

/**
 * Fragment the represents a simple icon.
 */
public class Icon {

    public static IconFragment $(String icon) {
        return new IconFragment (icon);
    }

    public static IconFragment $(IDomInsertableContainer<?> parent, String icon) {
        IconFragment frg = $ (icon);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class IconFragment extends BaseFragment<IconFragment> {

        /**
         * See constructor.
         */
        private String icon;

        /**
         * See {@link #style(String)}.
         */
        private String style;

        /**
         * See {@link #size(Length)}.
         */
        private Length size;

        /**
         * See {@link #color(Color)}.
         */
        private Color color;

        /**
         * See {@link #onclick(Invoker)}.
         */
        private Invoker onclick;

        /**
         * Construct with the CSS of an icon.
         * 
         * @param icon
         *             the icon CSS.
         */
        public IconFragment(String icon) {
            this.icon = icon;
        }

        /**
         * Any additional style to apply.
         * 
         * @param style
         *              the css style to apply.
         * @return the icon instance.
         */
        public IconFragment style(String style) {
            this.style = style;
            return this;
        }

        /**
         * The font size.
         * 
         * @param size
         *              the size to apply.
         * @return the icon instance.
         */
        public IconFragment size(Length size) {
            this.size = size;
            return this;
        }

        /**
         * The font color.
         * 
         * @param color
         *              the color to apply.
         * @return the icon instance.
         */
        public IconFragment color(Color color) {
            this.color = color;
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public IconFragment onclick(Invoker onclick) {
            this.onclick = onclick;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (icon == null)
                return null;
            ElementBuilder em = Em.$ (parent);
            em.style ("juiIcon", icon);
            if (style != null)
                em.style (style);
            if (size != null)
                em.css (CSS.FONT_SIZE, size);
            if (color != null)
                em.css (CSS.COLOR, color);
            if (onclick != null) {
                em.style ("clickable");
                em.onclick (e -> onclick.invoke());
            }
            return em;
        }

    }
    
}
